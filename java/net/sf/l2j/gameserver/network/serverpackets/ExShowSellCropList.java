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

import net.sf.l2j.gameserver.instancemanager.CastleManorManager;
import net.sf.l2j.gameserver.model.item.instance.ItemInstance;
import net.sf.l2j.gameserver.model.itemcontainer.PcInventory;
import net.sf.l2j.gameserver.model.manor.CropProcure;
import net.sf.l2j.gameserver.model.manor.Seed;

public class ExShowSellCropList extends L2GameServerPacket
{
	private int _manorId = 1;
	private final Map<Integer, ItemInstance> _cropsItems;
	private final Map<Integer, CropProcure> _castleCrops;
	
	public ExShowSellCropList(PcInventory inventory, int manorId)
	{
		_manorId = manorId;
		_castleCrops = new HashMap<>();
		_cropsItems = new HashMap<>();
		
		for (int cropId : CastleManorManager.getInstance().getCropIds())
		{
			final ItemInstance item = inventory.getItemByItemId(cropId);
			if (item != null)
				_cropsItems.put(cropId, item);
		}
		
		for (CropProcure crop : CastleManorManager.getInstance().getCropProcure(_manorId, false))
		{
			if (_cropsItems.containsKey(crop.getId()) && crop.getAmount() > 0)
				_castleCrops.put(crop.getId(), crop);
		}
	}
	
	@Override
	public void writeImpl()
	{
		writeC(0xFE);
		writeH(0x21);
		
		writeD(_manorId);
		writeD(_cropsItems.size());
		
		for (ItemInstance item : _cropsItems.values())
		{
			final Seed seed = CastleManorManager.getInstance().getSeedByCrop(item.getItemId());
			
			writeD(item.getObjectId());
			writeD(item.getItemId());
			writeD(seed.getLevel());
			writeC(1);
			writeD(seed.getReward(1));
			writeC(1);
			writeD(seed.getReward(2));
			
			if (_castleCrops.containsKey(item.getItemId()))
			{
				final CropProcure crop = _castleCrops.get(item.getItemId());
				writeD(_manorId);
				writeD(crop.getAmount());
				writeD(crop.getPrice());
				writeC(crop.getReward());
			}
			else
			{
				writeD(0xFFFFFFFF);
				writeD(0);
				writeD(0);
				writeC(0);
			}
			writeD(item.getCount());
		}
	}
}