/*
 * PitHelper - A Hypixel The Pit Helper Mod.
 * Copyright (C) 2026 Christian Steenkamp
 *
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU Affero General Public License as
 * published by the Free Software Foundation, either version 3 of the
 * License, or (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU Affero General Public License for more details.
 *
 * You should have received a copy of the GNU Affero General Public License
 * along with this program.  If not, see <https://www.gnu.org/licenses/>.
 */

package io.github.christechs.pithelper.ui.screen;

import io.github.christechs.clayj.config.ImageConfigBuilder;
import io.github.christechs.clayj.enums.LayoutAlignmentY;
import io.github.christechs.config.ConfigManager;
import io.github.christechs.pithelper.api.PitHelperAPI;
import io.github.christechs.pithelper.config.PitConfig;
import io.github.christechs.pithelper.data.EventFetcher;
import io.github.christechs.pithelper.data.PitEvent;
import io.github.christechs.pithelper.ui.base.AutoConfigScreen;
import io.github.christechs.pithelper.ui.base.IMenuTab;
import io.github.christechs.pithelper.ui.components.ClayComponents;
import io.github.christechs.pithelper.ui.hud.EventOverlay;
import io.github.christechs.pithelper.ui.utils.IconCache;
import io.github.christechs.pithelper.ui.utils.PitUIUtil;
import io.github.christechs.pithelper.ui.xml.ClayXmlEngine;
import io.github.christechs.pithelper.ui.xml.XmlContext;
import io.github.christechs.pithelper.ui.xml.nodes.XmlNode;
import io.github.christechs.pithelper.compat.McCompat;
import io.github.christechs.pithelper.utils.ChatUtil;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.screens.Screen;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import static io.github.christechs.clayj.ClayJ.*;
import static io.github.christechs.clayj.enums.LayoutAlignmentX.LEFT;
import static io.github.christechs.clayj.enums.LayoutDirection.LEFT_TO_RIGHT;
import static io.github.christechs.clayj.enums.SizingType.FIXED;
import static io.github.christechs.clayj.enums.SizingType.GROW;

public class PitHelperMenu extends AutoConfigScreen {

    public Screen screenToOpen = null;

    public PitHelperMenu() {
        super("PitHelper");
        loadConfigCategories();
        addTab(new LiveEventsTab(this));
        addTab(new ApiTab(this));
        addTab(new SocialTab(this));
        addTab(new DevToolsTab());
    }

    @Override
    protected void onPostBuildLayout() {
        if (this.screenToOpen != null) {
			this.mc.gui.setScreen(this.screenToOpen);
            this.screenToOpen = null;
        }
    }

    @Override
    public void onClose() {
        super.onClose();
        ConfigManager.save();
    }

    private static class ApiTab implements IMenuTab {
        private final PitHelperMenu parent;
        private final XmlNode apiXmlNode;
        private final XmlContext ctx;
        private String keyStatusMessage = "Idle";
        private boolean isHidden = true;

        public ApiTab(PitHelperMenu parent) {
            this.parent = parent;
            apiXmlNode = ClayXmlEngine.load("ui/api_tab.xml");
            ctx = new XmlContext();

            ctx.bindString("scrollHeight", () -> String.valueOf(parent.currentContentHeight - 40));
            ctx.bindString("limitText", () -> "Bucket Capacity: " + PitHelperAPI.lastLimit + " tokens/min");
            ctx.bindString("remainingText", () -> "Remaining: " + PitHelperAPI.lastRemaining);
            ctx.bindString("resetText", () -> "Bucket Refills in: " + PitHelperAPI.lastResetSec + "s");
            ctx.bindString("keyStatus", () -> "Status: " + keyStatusMessage);
            ctx.bindString("keyDisplay", () -> PitConfig.api().pitHelperApiKey == null || PitConfig.api().pitHelperApiKey.isEmpty()
                    ? "No Key Set (IP Rate Limit: 20/min)"
                    : (isHidden ? "••••••••-••••-••••-••••-••••••••••••" : PitConfig.api().pitHelperApiKey));

            ctx.bindAction("openExplorer", () -> parent.screenToOpen = new ApiExplorerScreen(parent));

            ctx.bindAction("generateKey", () -> {
                keyStatusMessage = "§eRegistering with server...";
                PitHelperAPI.generateNewKey().whenComplete((newKey, error) -> {
                    if (error != null) keyStatusMessage = "§c" + error.getMessage();
                    else {
                        PitConfig.api().pitHelperApiKey = newKey;
                        ConfigManager.save();
                        keyStatusMessage = "§aKey registered successfully!";
                    }
                });
            });

            ctx.bindCustomRenderer("Btn_ToggleHideKey", () -> {
                if (PitConfig.api().pitHelperApiKey != null && !PitConfig.api().pitHelperApiKey.isEmpty())
                    ClayComponents.button("Btn_ToggleHideLocal", isHidden ? "Show" : "Hide", () -> isHidden = !isHidden);
            });
            ctx.bindCustomRenderer("Btn_CopyKey", () -> {
                if (PitConfig.api().pitHelperApiKey != null && !PitConfig.api().pitHelperApiKey.isEmpty())
                    ClayComponents.button("Btn_CopyLocalKey", "Copy", () -> {
                        McCompat.setClipboard(PitConfig.api().pitHelperApiKey);
                        keyStatusMessage = "§aCopied to clipboard!";
                    });
            });
            ctx.bindCustomRenderer("Btn_ClearKey", () -> {
                if (PitConfig.api().pitHelperApiKey != null && !PitConfig.api().pitHelperApiKey.isEmpty()) {
                    ClayComponents.button("Btn_ClearLocalKey", "Clear", () -> {
                        PitConfig.api().pitHelperApiKey = "";
                        ConfigManager.save();
                        keyStatusMessage = "§eKey cleared.";
                    });
                }
            });
        }

        @Override
        public String getName() {
            return "PitHelper API";
        }

        @Override
        public void draw() {
            if (apiXmlNode != null) apiXmlNode.render(ctx);
        }
    }

    private static class LiveEventsTab implements IMenuTab {
        private final XmlNode xmlNode;
        private final XmlContext ctx;

        public LiveEventsTab(PitHelperMenu parent) {
            xmlNode = ClayXmlEngine.load("ui/live_events_tab.xml");
            ctx = new XmlContext();

            ctx.bindString("scrollHeight", () -> String.valueOf(parent.currentContentHeight - 40));
            ctx.bindAction("refreshEvents", EventFetcher::fetchAsync);

            ctx.bindCustomRenderer("EventList", () -> {
                long currentTime = System.currentTimeMillis();
                if (EventFetcher.activeEvent != null) {
                    PitEvent ae = EventFetcher.activeEvent;
                    long actualEnd = ae.timestamp + ae.eventType.startOffset + ae.eventType.duration;
                    if (currentTime < actualEnd) drawRow(ae, actualEnd - currentTime, true, 999);
                }
                if (EventFetcher.cachedEvents != null) {
                    for (int i = 0; i < EventFetcher.cachedEvents.size(); i++) {
                        PitEvent e = EventFetcher.cachedEvents.get(i);
                        long actualStart = e.timestamp + e.eventType.startOffset;
                        if (actualStart >= currentTime) {
                            drawRow(e, actualStart - currentTime, false, i);
                        }
                    }
                }
            });
        }

        @Override
        public String getName() {
            return "Live Events";
        }

        @Override
        public void draw() {
            if (xmlNode != null) xmlNode.render(ctx);
        }

        private void drawRow(PitEvent e, long timeDiff, boolean isActive, int index) {
            boolean rowHover = pointerOver("LiveRow_" + index);
            el(decl()
                            .id("LiveRow_" + index)
                            .bg(
                                    isActive ? (rowHover ? 100 : 80) : (rowHover ? 60 : 45),
                                    isActive ? 180 : (rowHover ? 60 : 45),
                                    isActive ? (rowHover ? 100 : 80) : 50, 255
                            )
                            .radius(4)
                            .layout(layout().sizing(GROW, 0, FIXED, 36).dir(LEFT_TO_RIGHT).align(LEFT, LayoutAlignmentY.CENTER).padding(10, 10).gap(15)),
                    () -> {
                        el(decl().image(new ImageConfigBuilder().data(IconCache.get(e.event)).sourceDim(16, 16)).layout(layout().sizing(FIXED, 16, FIXED, 16)), () -> {
                        });
                        text((isActive ? "ACTIVE: " : "") + e.event, txt().size(1).color(isActive ? 255 : 200, isActive ? 255 : 200, isActive ? 255 : 200, 255));
                        el(decl().layout(layout().sizing(GROW, 0, FIXED, 10)), () -> {
                        });
                        text(EventOverlay.formatTimeVerbose(timeDiff), txt().size(1).color(isActive ? 200 : 150, 255, isActive ? 200 : 150, 255));
                    });
        }
    }

    private static class SocialTab implements IMenuTab {
        private final PitHelperMenu parent;
        private final XmlNode xmlNode;
        private final XmlContext ctx;

        private boolean showFriends = true;
        private String socialInput = "";
        private int socialPage = 0;

        public SocialTab(PitHelperMenu parent) {
            this.parent = parent;
            xmlNode = ClayXmlEngine.load("ui/social_tab.xml");
            ctx = new XmlContext();

            ctx.bindString("scrollHeight", () -> String.valueOf(parent.currentContentHeight - 40));

            ctx.bindInput("socialInput", () -> socialInput, val -> socialInput = val);

            ctx.bindAction("addFriend", () -> {
                if (!socialInput.isEmpty()) {
                    Minecraft.getInstance().player.connection.sendCommand("pitfriend " + socialInput);
                    socialInput = "";
                }
            });

            ctx.bindAction("addEnemy", () -> {
                if (!socialInput.isEmpty()) {
                    Minecraft.getInstance().player.connection.sendCommand("pitenemy " + socialInput);
                    socialInput = "";
                }
            });

            ctx.bindCustomRenderer("SocialTabs", () -> {
                ClayComponents.tab("SubTab_Friends", "Friends (" + PitConfig.social().friends.size() + ")", showFriends, () -> {
                    showFriends = true;
                    socialPage = 0;
                });
                ClayComponents.tab("SubTab_Enemies", "Enemies (" + PitConfig.social().enemies.size() + ")", !showFriends, () -> {
                    showFriends = false;
                    socialPage = 0;
                });
            });

            ctx.bindCustomRenderer("SocialList", () -> {
                Map<String, String> targetMap = showFriends ? PitConfig.social().friends : PitConfig.social().enemies;

                if (targetMap.isEmpty()) {
                    text("List is empty.", txt().size(1).color(150, 150, 150, 255));
                } else {
                    List<Map.Entry<String, String>> safeEntries = new ArrayList<>(targetMap.entrySet());

                    int itemsPerPage = 8;
                    int totalItems = safeEntries.size();
                    int totalPages = (int) Math.ceil((double) totalItems / itemsPerPage);
                    if (socialPage >= totalPages && totalPages > 0) socialPage = totalPages - 1;

                    int startIdx = socialPage * itemsPerPage;
                    int endIdx = Math.min(startIdx + itemsPerPage, totalItems);

                    for (int i = startIdx; i < endIdx; i++) {
                        Map.Entry<String, String> entry = safeEntries.get(i);
                        el(decl().bg(35, 38, 45, 255).radius(4).layout(layout().sizing(GROW, 0, FIXED, 30).padding(10, 0).dir(LEFT_TO_RIGHT).align(LEFT, LayoutAlignmentY.CENTER)), () -> {
                            text((showFriends ? "§a" : "§c") + entry.getValue(), txt().size(1));
                            el(decl().layout(layout().sizing(GROW, 0, FIXED, 0)), () -> {
                            });
                            ClayComponents.button("Btn_Del_" + entry.getKey(), "Remove", () -> {
                                targetMap.remove(entry.getKey());
                                ConfigManager.save();
                            });
                        });
                    }

                    PitUIUtil.drawPageNav(socialPage, totalPages, "SocialPage", () -> socialPage--, () -> socialPage++);
                }
            });
        }

        @Override
        public String getName() {
            return "Social Manager";
        }

        @Override
        public void draw() {
            if (xmlNode != null) xmlNode.render(ctx);
        }
    }

    private static class DevToolsTab implements IMenuTab {
        private final XmlNode xmlNode;
        private final XmlContext ctx;

        public DevToolsTab() {
            xmlNode = ClayXmlEngine.load("ui/dev_tools_tab.xml");
            ctx = new XmlContext();

            ctx.bindAction("testVis", () -> io.github.christechs.pithelper.features.NotificationManager.add(null, "Mock notification test.", 5000));
            ctx.bindAction("testChat", () -> ChatUtil.simulateChat("§6§l[PitHelper] §eTEST EVENT §7starts in §a0m 0s§7!"));
            ctx.bindAction("testMath", () -> ChatUtil.simulateChat("§d§lQUICK MATHS! §eSolve: (2+8)x5"));
            ctx.bindAction("testDeath", () -> ChatUtil.simulateChat("§c§lDEATH! §7by §c[§4§l81§c] §6TEST_PLAYER §e§lVIEW RECAP"));
        }

        @Override
        public String getName() {
            return "Dev Tools";
        }

        @Override
        public void draw() {
            if (xmlNode != null) xmlNode.render(ctx);
        }
    }
}