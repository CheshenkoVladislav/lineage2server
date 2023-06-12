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
package net.sf.l2j.gameserver.datatables;

import java.io.File;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.logging.Logger;

import net.sf.l2j.gameserver.model.L2Skill;
import net.sf.l2j.gameserver.skills.DocumentSkill;
import net.sf.l2j.gameserver.skills.effects.EffectTemplate;

public class SkillTable
{
	private static final Logger _log = Logger.getLogger(SkillTable.class.getName());
	
	private static final Map<Integer, L2Skill> _skills = new HashMap<>();
	private static final Map<Integer, Integer> _skillMaxLevel = new HashMap<>();
	
	private static final L2Skill[] _heroSkills = new L2Skill[5];
	private static final int[] _heroSkillsId =
	{
		395,
		396,
		1374,
		1375,
		1376
	};
	
	private static final L2Skill[] _nobleSkills = new L2Skill[8];
	private static final int[] _nobleSkillsId =
	{
		325,
		326,
		327,
		1323,
		1324,
		1325,
		1326,
		1327
	};
	
	private int[] buffSkillIds;
	
	public static SkillTable getInstance()
	{
		return SingletonHolder._instance;
	}
	
	protected SkillTable()
	{
		load();
	}
	
	private void load()
	{
		initBuffsWithCustomTime();
		final File dir = new File("./data/xml/skills");
		for (File file : dir.listFiles())
		{
			DocumentSkill doc = new DocumentSkill(file);
			doc.parse();
			
			for (L2Skill skill : doc.getSkills())
			{
				if (Arrays.binarySearch(buffSkillIds, skill.getId()) > -1)
				{
					List<EffectTemplate> effectTemplates = skill.getEffectTemplates();
					if (effectTemplates.size() > 0)
					{
						for (EffectTemplate effectTemplate : effectTemplates)
						{
							if (effectTemplate.period > 0)
							{
								effectTemplate.period = 4200;
							}
						}
					}
				}
				_skills.put(getSkillHashCode(skill), skill);
			}
		}
		
		_log.info("SkillTable: Loaded " + _skills.size() + " skills.");
		
		// Stores max level of skills in a map for future uses.
		for (final L2Skill skill : _skills.values())
		{
			// Only non-enchanted skills
			final int skillLvl = skill.getLevel();
			if (skillLvl < 99)
			{
				final int skillId = skill.getId();
				final int maxLvl = getMaxLevel(skillId);
				
				if (skillLvl > maxLvl)
					_skillMaxLevel.put(skillId, skillLvl);
			}
		}
		
		// Loading FrequentSkill enumeration values
		for (FrequentSkill sk : FrequentSkill.values())
			sk._skill = getInfo(sk._id, sk._level);
		
		for (int i = 0; i < _heroSkillsId.length; i++)
			_heroSkills[i] = getInfo(_heroSkillsId[i], 1);
		
		for (int i = 0; i < _nobleSkills.length; i++)
			_nobleSkills[i] = getInfo(_nobleSkillsId[i], 1);
	}
	
	private void initBuffsWithCustomTime()
	{
		String skillDurationString = "271,4200;272,4200;273,4200;274,4200;275,4200;276,4200;277,4200;307,4200;309,4200;310,4200;311,4200;366,4200;367,4200;530,4200;264,4200;265,4200;266,4200;267,4200;268,4200;269,4200;270,4200;304,4200;305,4200;306,4200;308,4200;349,4200;363,4200;364,4200;437,4200;529,4200;1002,4200;1003,4200;1005,4200;1006,4200;1007,4200;1009,4200;1010,4200;1229,4200;1251,4200;1252,4200;1253,4200;1284,4200;1308,4200;1309,4200;1310,4200;1362,4200;1363,4200;1390,4200;1391,4200;1413,4200;1461,4200;1353,4200;1311,4200;1307,4200;1204,4200;1085,4200;1078,4200;1077,4200;1062,4200;1044,4200;1043,4200;1035,4200;1068,4200;1040,4200;1073,4200;1191,4200;1189,4200;1182,4200;1033,4200;1259,4200;1032,4200;1036,4200;1045,4200;1048,4200;1086,4200;1240,4200;1242,4200;1243,4200;1388,4200;1389,4200;1392,4200;1393,4200;1352,4200;1355,4200;1356,4200;1357,4200;1087,4200;1257,4200;1397,4200;1304,4200;1303,4200;1354,4200;1059,4200;1268";
		String [] skillDurations = skillDurationString.split(",4200;");
		buffSkillIds = Arrays.stream(skillDurations).mapToInt(Integer::parseInt).toArray();
		Arrays.sort(buffSkillIds);
	}
	
	public void reload()
	{
		_skills.clear();
		_skillMaxLevel.clear();
		
		load();
	}
	
	/**
	 * Provides the skill hash
	 * @param skill The L2Skill to be hashed
	 * @return SkillTable.getSkillHashCode(skill.getId(), skill.getLevel())
	 */
	public static int getSkillHashCode(L2Skill skill)
	{
		return getSkillHashCode(skill.getId(), skill.getLevel());
	}
	
	/**
	 * Centralized method for easier change of the hashing sys
	 * @param skillId    The Skill Id
	 * @param skillLevel The Skill Level
	 * @return The Skill hash number
	 */
	public static int getSkillHashCode(int skillId, int skillLevel)
	{
		return skillId * 256 + skillLevel;
	}
	
	public L2Skill getInfo(int skillId, int level)
	{
		return _skills.get(getSkillHashCode(skillId, level));
	}
	
	public int getMaxLevel(int skillId)
	{
		final Integer maxLevel = _skillMaxLevel.get(skillId);
		return (maxLevel != null) ? maxLevel : 0;
	}
	
	/**
	 * @param addNoble if true, will add also Advanced headquarters.
	 * @return an array with siege skills.
	 */
	public L2Skill[] getSiegeSkills(boolean addNoble)
	{
		L2Skill[] temp = new L2Skill[2 + (addNoble ? 1 : 0)];
		int i = 0;
		
		temp[i++] = _skills.get(SkillTable.getSkillHashCode(246, 1));
		temp[i++] = _skills.get(SkillTable.getSkillHashCode(247, 1));
		
		if (addNoble)
			temp[i++] = _skills.get(SkillTable.getSkillHashCode(326, 1));
		
		return temp;
	}
	
	public static L2Skill[] getHeroSkills()
	{
		return _heroSkills;
	}
	
	public static boolean isHeroSkill(int skillid)
	{
		for (int id : _heroSkillsId)
			if (id == skillid)
				return true;
			
		return false;
	}
	
	public static L2Skill[] getNobleSkills()
	{
		return _nobleSkills;
	}
	
	/**
	 * Enum to hold some important references to frequently used (hardcoded) skills in core
	 * @author DrHouse
	 */
	public static enum FrequentSkill
	{
		LUCKY(194, 1),
		SEAL_OF_RULER(246, 1),
		BUILD_HEADQUARTERS(247, 1),
		STRIDER_SIEGE_ASSAULT(325, 1),
		DWARVEN_CRAFT(1321, 1),
		COMMON_CRAFT(1322, 1),
		LARGE_FIREWORK(2025, 1),
		SPECIAL_TREE_RECOVERY_BONUS(2139, 1),
		
		ANTHARAS_JUMP(4106, 1),
		ANTHARAS_TAIL(4107, 1),
		ANTHARAS_FEAR(4108, 1),
		ANTHARAS_DEBUFF(4109, 1),
		ANTHARAS_MOUTH(4110, 1),
		ANTHARAS_BREATH(4111, 1),
		ANTHARAS_NORMAL_ATTACK(4112, 1),
		ANTHARAS_NORMAL_ATTACK_EX(4113, 1),
		ANTHARAS_SHORT_FEAR(5092, 1),
		ANTHARAS_METEOR(5093, 1),
		
		RAID_CURSE(4215, 1),
		WYVERN_BREATH(4289, 1),
		ARENA_CP_RECOVERY(4380, 1),
		RAID_CURSE2(4515, 1),
		VARKA_KETRA_PETRIFICATION(4578, 1),
		FAKE_PETRIFICATION(4616, 1),
		THE_VICTOR_OF_WAR(5074, 1),
		THE_VANQUISHED_OF_WAR(5075, 1),
		BLESSING_OF_PROTECTION(5182, 1),
		FIREWORK(5965, 1);
		
		protected final int _id;
		protected final int _level;
		protected L2Skill _skill = null;
		
		private FrequentSkill(int id, int level)
		{
			_id = id;
			_level = level;
		}
		
		public L2Skill getSkill()
		{
			return _skill;
		}
	}
	
	private static class SingletonHolder
	{
		protected static final SkillTable _instance = new SkillTable();
	}
}