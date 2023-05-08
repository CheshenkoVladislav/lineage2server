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
package net.sf.l2j.gameserver.network.serverpackets;

import java.util.HashMap;
import java.util.Map;
import java.util.Set;

import net.sf.l2j.gameserver.instancemanager.CastleManorManager;
import net.sf.l2j.gameserver.model.manor.CropProcure;
import net.sf.l2j.gameserver.model.manor.Seed;

public class ExShowCropSetting extends L2GameServerPacket
{
	private final int _manorId;
	private final Set<Seed> _seeds;
	private final Map<Integer, CropProcure> _current = new HashMap<>();
	private final Map<Integer, CropProcure> _next = new HashMap<>();
	
	public ExShowCropSetting(int manorId)
	{
		final CastleManorManager manor = CastleManorManager.getInstance();
		
		_manorId = manorId;
		_seeds = manor.getSeedsForCastle(_manorId);
		for (Seed s : _seeds)
		{
			// Current period
			CropProcure cp = manor.getCropProcure(manorId, s.getCropId(), false);
			if (cp != null)
				_current.put(s.getCropId(), cp);
			
			// Next period
			cp = manor.getCropProcure(manorId, s.getCropId(), true);
			if (cp != null)
				_next.put(s.getCropId(), cp);
		}
	}
	
	@Override
	public void writeImpl()
	{
		writeC(0xFE); // Id
		writeH(0x20); // SubId
		
		writeD(_manorId); // manor id
		writeD(_seeds.size()); // size
		
		CropProcure cp;
		for (Seed s : _seeds)
		{
			writeD(s.getCropId()); // crop id
			writeD(s.getLevel()); // seed level
			writeC(1);
			writeD(s.getReward(1)); // reward 1 id
			writeC(1);
			writeD(s.getReward(2)); // reward 2 id
			
			writeD(s.getCropLimit()); // next sale limit
			writeD(0); // ???
			writeD(s.getCropMinPrice()); // min crop price
			writeD(s.getCropMaxPrice()); // max crop price
			
			// Current period
			if (_current.containsKey(s.getCropId()))
			{
				cp = _current.get(s.getCropId());
				writeD(cp.getStartAmount()); // buy
				writeD(cp.getPrice()); // price
				writeC(cp.getReward()); // reward
			}
			else
			{
				writeD(0);
				writeD(0);
				writeC(0);
			}
			// Next period
			if (_next.containsKey(s.getCropId()))
			{
				cp = _next.get(s.getCropId());
				writeD(cp.getStartAmount()); // buy
				writeD(cp.getPrice()); // price
				writeC(cp.getReward()); // reward
			}
			else
			{
				writeD(0);
				writeD(0);
				writeC(0);
			}
		}
	}
}