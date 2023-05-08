/*
 * This program is free software: you can redistribute it and/or modify it under
 * the terms of the GNU General Public License as published by the Free Software
 * Foundation, either version 3 of the License, or (at your option) any later
 * version.
 * 
 * This program is distributed in the hope that it will be useful, but WITHOUT
 * ANY WARRANTY; without even the implied warranty of MERCHANTABILITY or FITNESS
 * FOR A PARTICULAR PURPOSE. See the GNU General Public License for more
 * details.
 * 
 * You should have received a copy of the GNU General Public License along with
 * this program. If not, see <http://www.gnu.org/licenses/>.
 */
package net.sf.l2j.gameserver.taskmanager;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

import net.sf.l2j.commons.concurrent.ThreadPool;

import net.sf.l2j.gameserver.model.actor.instance.L2PcInstance;
import net.sf.l2j.gameserver.network.SystemMessageId;
import net.sf.l2j.gameserver.network.serverpackets.SetupGauge;
import net.sf.l2j.gameserver.network.serverpackets.SetupGauge.GaugeColor;
import net.sf.l2j.gameserver.network.serverpackets.SystemMessage;
import net.sf.l2j.gameserver.skills.Stats;

/**
 * Updates {@link L2PcInstance} drown timer and reduces {@link L2PcInstance} HP, when drowning.
 */
public final class WaterTaskManager implements Runnable
{
	private final Map<L2PcInstance, Long> _players = new ConcurrentHashMap<>();
	
	public static final WaterTaskManager getInstance()
	{
		return SingletonHolder._instance;
	}
	
	protected WaterTaskManager()
	{
		// Run task each second.
		ThreadPool.scheduleAtFixedRate(this, 1000, 1000);
	}
	
	/**
	 * Adds {@link L2PcInstance} to the WaterTask.
	 * @param player : {@link L2PcInstance} to be added and checked.
	 */
	public final void add(L2PcInstance player)
	{
		if (!player.isDead() && !_players.containsKey(player))
		{
			final int time = (int) player.calcStat(Stats.BREATH, 60000 * player.getRace().getBreathMultiplier(), player, null);
			
			_players.put(player, System.currentTimeMillis() + time);
			
			player.sendPacket(new SetupGauge(GaugeColor.CYAN, time));
		}
	}
	
	/**
	 * Removes {@link L2PcInstance} from the WaterTask.
	 * @param player : L2PcInstance to be removed.
	 */
	public final void remove(L2PcInstance player)
	{
		if (_players.remove(player) != null)
			player.sendPacket(new SetupGauge(GaugeColor.CYAN, 0));
	}
	
	@Override
	public final void run()
	{
		// List is empty, skip.
		if (_players.isEmpty())
			return;
		
		// Get current time.
		final long time = System.currentTimeMillis();
		
		// Loop all players.
		for (Map.Entry<L2PcInstance, Long> entry : _players.entrySet())
		{
			// Time has not passed yet, skip.
			if (time < entry.getValue())
				continue;
			
			// Get player.
			final L2PcInstance player = entry.getKey();
			
			// Reduce 1% of HP per second.
			final double hp = player.getMaxHp() / 100.0;
			player.reduceCurrentHp(hp, player, false, false, null);
			player.sendPacket(SystemMessage.getSystemMessage(SystemMessageId.DROWN_DAMAGE_S1).addNumber((int) hp));
		}
	}
	
	private static class SingletonHolder
	{
		protected static final WaterTaskManager _instance = new WaterTaskManager();
	}
}