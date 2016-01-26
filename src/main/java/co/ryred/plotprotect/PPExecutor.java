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
import com.intellectualcrafters.plot.object.Plot;
import org.bukkit.ChatColor;
import org.bukkit.OfflinePlayer;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

import java.util.concurrent.TimeUnit;

/**
 * Created by Cory Redmond on 26/01/2016.
 *
 * @author Cory Redmond <ace@ac3-servers.eu>
 */
public class PPExecutor implements CommandExecutor {

    private final PlotProtectPlugin plugin;

    public PPExecutor( PlotProtectPlugin plugin ) {
        this.plugin = plugin;
    }

    @Override
    public boolean onCommand(CommandSender sender, Command cmd, String label, String[] args) {

        Cooldown cooldown = Cooldown.get(PPExecutor.class, 1L, TimeUnit.MINUTES);

        if(!(sender instanceof Player)) {
            sender.sendMessage(c("&eOnly players can use these commands!"));
            return true;
        }

        Player pSender = ((Player) sender);

        if( cooldown.isChilling( pSender.getUniqueId() ) ) {
            sender.sendMessage(c("&cHey there! Calm down..."));
            sender.sendMessage(c("&Wait " + cooldown.getCooldownTimeFormatted(pSender.getUniqueId())));
            return true;
        }

        if( args.length >= 1 && args[0].equalsIgnoreCase("add") ) {
            if(args.length == 3) {
                OfflinePlayer player = plugin.getServer().getOfflinePlayer(args[1]);
                if( plugin.password.equals(args[2]) ) {
                    if( player == null || !player.hasPlayedBefore() ) {
                        sender.sendMessage(c("&eThat player couldn't be found or hasn't played before!"));
                        return true;
                    } else {
                        plugin.allowedPlayers.add(player.getUniqueId());
                        sender.sendMessage(c("&aSuccessfully allowed " + player.getName()));
                    }
                } else {
                    sender.sendMessage(c("&eThe password you supplied was incorrect!"));
                    cooldown.addChilling(pSender.getUniqueId());
                    return true;
                }
            } else {
                sender.sendMessage(c("&eBad usage!"));
                sender.sendMessage(c("&e  /" + label + " add <playername> <password>"));
                return true;
            }
        } else if( args.length >= 1 && args[0].equalsIgnoreCase("remove") ) {
            if(args.length == 3) {
                OfflinePlayer player = plugin.getServer().getOfflinePlayer(args[1]);
                if( plugin.password.equals(args[2]) ) {
                    if( player == null || !player.hasPlayedBefore() ) {
                        sender.sendMessage(c("&eThat player couldn't be found or hasn't played before!"));
                        return true;
                    } else {
                        plugin.allowedPlayers.remove(player.getUniqueId());
                        sender.sendMessage(c("&aSuccessfully removed " + player.getName()));
                    }
                } else {
                    sender.sendMessage(c("&eThe password you supplied was incorrect!"));
                    cooldown.addChilling(pSender.getUniqueId());
                    return true;
                }
            } else {
                sender.sendMessage(c("&eBad usage!"));
                sender.sendMessage(c("&e  /" + label + " remove <playername> <password>"));
                return true;
            }
        } else if( args.length >= 1 && args[0].equalsIgnoreCase("protect") ) {
            if(args.length == 2) {
                if( plugin.password.equals(args[1]) ) {
                    Plot plot = plugin.getPlot(((Player) sender).getLocation());
                    if( plot != null && plot.getId() != null ) {
                        plugin.locked_plots.add(plot.getId().toString());
                        plugin.saveLockedPlots();
                        sender.sendMessage(c("&aSuccessfully protected the plot."));
                        return true;
                    } else {
                        sender.sendMessage(c("&eYou're not currently on a plot.."));
                        sender.sendMessage(c("&e  Please stand on the plot you wish to protect."));
                        return true;
                    }
                } else {
                    sender.sendMessage(c("&eThe password you supplied was incorrect!"));
                    cooldown.addChilling(pSender.getUniqueId());
                    return true;
                }
            } else {
                sender.sendMessage(c("&eBad usage!"));
                sender.sendMessage(c("&e  /" + label + " protect <password>"));
                return true;
            }
        } else if( args.length >= 1 && args[0].equalsIgnoreCase("unprotect") ) {
            if(args.length == 2) {
                if( plugin.password.equals(args[1]) ) {
                    Plot plot = plugin.getPlot(((Player) sender).getLocation());
                    if( plot != null && plot.getId() != null) {
                        plugin.locked_plots.remove(plot.getId().toString());
                        plugin.saveLockedPlots();
                        sender.sendMessage(c("&aSuccessfully unprotected the plot."));
                        return true;
                    } else {
                        sender.sendMessage(c("&eYou're not currently on a plot.."));
                        sender.sendMessage(c("&e  Please stand on the plot you wish to unprotect."));
                        return true;
                    }
                } else {
                    sender.sendMessage(c("&eThe password you supplied was incorrect!"));
                    cooldown.addChilling(pSender.getUniqueId());
                    return true;
                }
            } else {
                sender.sendMessage(c("&eBad usage!"));
                sender.sendMessage(c("&e  /" + label + " unprotect <password>"));
                return true;
            }
        } else if( args.length >= 1 && args[0].equalsIgnoreCase("auth") ) {
            if(args.length == 2) {
                if( plugin.password.equals(args[1]) ) {
                    plugin.allowedPlayers.add(((Player) sender).getUniqueId());
                    sender.sendMessage(c("&aSuccessfully authenticated."));
                } else {
                    sender.sendMessage(c("&eThe password you supplied was incorrect!"));
                    cooldown.addChilling(pSender.getUniqueId());
                    return true;
                }
            } else {
                sender.sendMessage(c("&eBad usage!"));
                sender.sendMessage(c("&e  /" + label + " auth <password>"));
                return true;
            }
        } else {
            sender.sendMessage( c( "&b&lPlotProtect Help." ) );

            sender.sendMessage( c( "&e&l/" + label + " add <playername> <password>" ) );
            sender.sendMessage( c( "&a&l  Allows <playername> to the bypass protection." ) );

            sender.sendMessage( c( "&e&l/" + label + " remove <playername> <password>" ) );
            sender.sendMessage( c( "&a&l  Removes <playername>'s ability to bypass protection." ) );

            sender.sendMessage( c( "&e&l/" + label + " protect <password>" ) );
            sender.sendMessage( c( "&a&l  Protects the current plot." ) );

            sender.sendMessage( c( "&e&l/" + label + " unprotect <password>" ) );
            sender.sendMessage( c( "&a&l  Unprotects the current plot." ) );

            sender.sendMessage( c( "&e&l/" + label + " auth <password>" ) );
            sender.sendMessage( c( "&a&l  Authenticate yourself to bypass protection." ) );
        }

        return true;
    }

    public static String c( String string ) {
        return ChatColor.translateAlternateColorCodes('&', string);
    }

}
