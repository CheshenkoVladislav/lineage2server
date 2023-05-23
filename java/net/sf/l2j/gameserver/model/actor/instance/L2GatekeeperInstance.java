package net.sf.l2j.gameserver.model.actor.instance;

import net.sf.l2j.Config;
import net.sf.l2j.commons.random.Rnd;
import net.sf.l2j.gameserver.instancemanager.GrandBossManager;
import net.sf.l2j.gameserver.instancemanager.ZoneManager;
import net.sf.l2j.gameserver.model.Location;
import net.sf.l2j.gameserver.model.actor.template.NpcTemplate;
import net.sf.l2j.gameserver.model.zone.type.L2BossZone;
import net.sf.l2j.gameserver.network.serverpackets.ActionFailed;
import net.sf.l2j.gameserver.network.serverpackets.NpcHtmlMessage;
import net.sf.l2j.gameserver.scripting.ScriptManager;
import net.sf.l2j.gameserver.scripting.scripts.ai.individual.Antharas;
import net.sf.l2j.gameserver.scripting.scripts.ai.individual.Valakas;

public class L2GatekeeperInstance extends L2NpcInstance
{
    
    public L2GatekeeperInstance(int objectId, NpcTemplate template)
    {
        super(objectId, template);
    }
    
    @Override
    public void showChatWindow(L2PcInstance player)
    {
        player.sendPacket(ActionFailed.STATIC_PACKET);
        String filename = "data/html/mods/teleporter/" + getNpcId() + ".html";
        
        final NpcHtmlMessage html = new NpcHtmlMessage(getObjectId());
        html.setFile(filename);
        html.replace("%objectId%", getObjectId());
        player.sendPacket(html);
    }
    
    @Override
    public void onBypassFeedback(L2PcInstance player, String command)
    {
        if (command.startsWith("valakas"))
        {
            ZoneManager.getInstance().getZoneById(110010, L2BossZone.class).allowPlayerEntry(player, 30);
            
            player.teleToLocation(204328, -111874, 70, 300);
            int status = GrandBossManager.getInstance().getBossStatus(Valakas.VALAKAS);
            if (status == Valakas.DORMANT)
            {
                GrandBossManager.getInstance().setBossStatus(Valakas.VALAKAS, Valakas.WAITING);
                ScriptManager.getInstance().getQuest("Valakas").startQuestTimer("beginning", Config.WAIT_TIME_VALAKAS, null, null, false);
            }
        }
        else if (command.startsWith("antharas"))
        {
            ZoneManager.getInstance().getZoneById(110001, L2BossZone.class).allowPlayerEntry(player, 30);
            
            player.teleToLocation(175300 + Rnd.get(-350, 350), 115180 + Rnd.get(-1000, 1000), -7709, 0);
            int status = GrandBossManager.getInstance().getBossStatus(Antharas.ANTHARAS);
            if (status == Antharas.DORMANT)
            {
                GrandBossManager.getInstance().setBossStatus(Antharas.ANTHARAS, Antharas.WAITING);
                ScriptManager.getInstance().getQuest("Antharas").startQuestTimer("beginning", Config.WAIT_TIME_ANTHARAS, null, null, false);
            }
        }
        else if (command.startsWith("baium"))
        {
            if (player.isFlying())
            {
                String filename = "data/html/scripts/teleports/GrandBossTeleporters/31862-05.htm";
                final NpcHtmlMessage html = new NpcHtmlMessage(getObjectId());
                html.setFile(filename);
                player.sendPacket(html);
            }
            else
            {
                ZoneManager.getInstance().getZoneById(110002, L2BossZone.class).allowPlayerEntry(player, 30);
                player.teleToLocation(new Location(113100, 14500, 10077), 0);
            }
        }
    }
}