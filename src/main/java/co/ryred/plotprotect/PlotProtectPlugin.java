/*
 * http://ryred.co/
 * ace[at]ac3-servers.eu
 *
 * =================================================================
 *
 * Copyright (c) 2016, Cory Redmond
 * All rights reserved.
 *
 * Redistribution and use in source and binary forms, with or without
 * modification, are permitted provided that the following conditions are met:
 *
 *  Redistributions of source code must retain the above copyright notice, this
 *   list of conditions and the following disclaimer.
 *
 *  Redistributions in binary form must reproduce the above copyright notice,
 *   this list of conditions and the following disclaimer in the documentation
 *   and/or other materials provided with the distribution.
 *
 *  Neither the name of PlotProtect nor the names of its
 *   contributors may be used to endorse or promote products derived from
 *   this software without specific prior written permission.
 *
 * THIS SOFTWARE IS PROVIDED BY THE COPYRIGHT HOLDERS AND CONTRIBUTORS "AS IS"
 * AND ANY EXPRESS OR IMPLIED WARRANTIES, INCLUDING, BUT NOT LIMITED TO, THE
 * IMPLIED WARRANTIES OF MERCHANTABILITY AND FITNESS FOR A PARTICULAR PURPOSE ARE
 * DISCLAIMED. IN NO EVENT SHALL THE COPYRIGHT HOLDER OR CONTRIBUTORS BE LIABLE
 * FOR ANY DIRECT, INDIRECT, INCIDENTAL, SPECIAL, EXEMPLARY, OR CONSEQUENTIAL
 * DAMAGES (INCLUDING, BUT NOT LIMITED TO, PROCUREMENT OF SUBSTITUTE GOODS OR
 * SERVICES; LOSS OF USE, DATA, OR PROFITS; OR BUSINESS INTERRUPTION) HOWEVER
 * CAUSED AND ON ANY THEORY OF LIABILITY, WHETHER IN CONTRACT, STRICT LIABILITY,
 * OR TORT (INCLUDING NEGLIGENCE OR OTHERWISE) ARISING IN ANY WAY OUT OF THE USE
 * OF THIS SOFTWARE, EVEN IF ADVISED OF THE POSSIBILITY OF SUCH DAMAGE.
 */

package co.ryred.plotprotect;

import co.ryred.red_commons.Cooldown;
import co.ryred.red_commons.Logs;
import com.intellectualcrafters.plot.PS;
import com.intellectualcrafters.plot.object.Plot;
import com.plotsquared.bukkit.util.BukkitUtil;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.block.Block;
import org.bukkit.event.Event;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.block.Action;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.plugin.java.JavaPlugin;

import java.io.File;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.UUID;
import java.util.concurrent.TimeUnit;

/**
 * Created by Cory Redmond on 26/01/2016.
 *
 * @author Cory Redmond <ace@ac3-servers.eu>
 */
public class PlotProtectPlugin extends JavaPlugin implements Listener {

    HashSet<String> locked_plots = new HashSet<>();
    HashSet<UUID> allowedPlayers = new HashSet<>();
    public String password = "Password123";

    @Override
    public void onLoad() {

        if(!new File( getDataFolder(), "config.yml" ).exists())
            saveDefaultConfig();

        Logs.get( PlotProtectPlugin.class, getLogger(), getConfig().getBoolean("debug", true) );

    }

    @Override
    public void onEnable() {

        getServer().getPluginManager().registerEvents(this, this);

        locked_plots.clear();
        locked_plots.addAll(getConfig().getStringList("locked-plots"));

        password = getConfig().getString( "password", "Password123" );
        
        getCommand( "PlotProtect" ).setExecutor( new PPExecutor( this ) );

        getServer().getScheduler().runTaskTimerAsynchronously(this, Cooldown::purge, 20*60L, 20*60L);

    }

    @EventHandler(priority = EventPriority.HIGHEST, ignoreCancelled = true)
    public void onInteract( PlayerInteractEvent event ) {

        if( !allowedPlayers.contains(event.getPlayer().getUniqueId() ) && event.getClickedBlock() != null && !isIgnoreType( event.getClickedBlock(), event.getAction() ) && PS.get().isPlotWorld( event.getClickedBlock().getLocation().getWorld().getName() ) ){

            Plot plot = getPlot(event.getClickedBlock().getLocation());
            if( plot != null && plot.getId() != null && locked_plots.contains(plot.getId().toString()) ) {
                event.setCancelled(true);
                event.setUseInteractedBlock(Event.Result.DENY);
                event.setUseItemInHand(Event.Result.DENY);

                Cooldown cooldown = Cooldown.get(PlotProtectPlugin.class, 1L, TimeUnit.MINUTES);

                if(!cooldown.isChilling(event.getPlayer().getUniqueId())) {
                    event.getPlayer().sendMessage(PPExecutor.c("&9This plot is protected, you can't build here!"));
                    cooldown.addChilling(event.getPlayer().getUniqueId());
                }

            }

        }

    }

    private boolean isIgnoreType(Block clickedBlock, Action action) {
        Material type = clickedBlock.getType();
        return (action == Action.RIGHT_CLICK_BLOCK || action == Action.RIGHT_CLICK_AIR) && (type == Material.WOOD_BUTTON || type == Material.STONE_BUTTON || type == Material.SIGN || type == Material.SIGN_POST || type == Material.WALL_SIGN || type == Material.LEVER || type == Material.BIRCH_DOOR || type == Material.ACACIA_DOOR || type == Material.DARK_OAK_DOOR || type == Material.IRON_DOOR || type == Material.JUNGLE_DOOR || type == Material.SPRUCE_DOOR || type == Material.TRAP_DOOR || type == Material.WOOD_DOOR || type == Material.GOLD_PLATE || type == Material.IRON_PLATE || type == Material.STONE_PLATE || type == Material.WOOD_PLATE || type == Material.IRON_TRAPDOOR || type == Material.IRON_DOOR_BLOCK || type == Material.WORKBENCH);
    }

    public Plot getPlot( Location location ) {
        return BukkitUtil.getLocation(location).getPlot();
    }

    public void saveLockedPlots() {

        ArrayList<String> arrayList = new ArrayList<>();
        arrayList.addAll(locked_plots);

        getConfig().set("locked-plots", arrayList);

        saveConfig();

    }
}
