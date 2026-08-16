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

package io.github.christechs.pithelper.ui.base;

import io.github.christechs.clayj.enums.LayoutAlignmentX;
import io.github.christechs.clayj.enums.LayoutAlignmentY;
import io.github.christechs.config.ConfigCategoryData;
import io.github.christechs.config.ConfigElement;
import io.github.christechs.config.ConfigManager;
import io.github.christechs.pithelper.ui.components.ClayComponents;
import io.github.christechs.pithelper.ui.components.ClayConfigUI;
import io.github.christechs.pithelper.ui.xml.ClayXmlEngine;
import io.github.christechs.pithelper.ui.xml.XmlContext;
import io.github.christechs.pithelper.ui.xml.nodes.XmlNode;

import java.util.ArrayList;
import java.util.List;

import static io.github.christechs.clayj.ClayJ.*;
import static io.github.christechs.clayj.enums.SizingType.FIXED;
import static io.github.christechs.clayj.enums.SizingType.GROW;

public abstract class AutoConfigScreen extends ClayScreen {

    protected final List<IMenuTab> tabs = new ArrayList<>();
    protected IMenuTab activeTab;
    protected String title;
    protected String globalSearchQuery = "";
    protected String categorySearchQuery = "";

    protected XmlNode xmlRoot;
    protected XmlNode searchResultsXml;
    protected XmlContext xmlContext;

    protected float currentContentHeight = 0;

    public AutoConfigScreen(String title) {
        this.title = title;
    }

    public void addTab(IMenuTab tab) {
        tabs.add(tab);
        if (activeTab == null) activeTab = tab;
    }

    @Override
    protected void init() {
        super.init();

        xmlRoot = ClayXmlEngine.load("ui/auto_config.xml");
        searchResultsXml = ClayXmlEngine.load("ui/search_results.xml");
        xmlContext = new XmlContext();

        xmlContext.bindString("menuTitle", () -> this.title);
        xmlContext.bindString("scrollHeight", () -> String.valueOf(currentContentHeight - 40));

        xmlContext.bindInput("globalSearchQuery", () -> globalSearchQuery, val -> globalSearchQuery = val);

        xmlContext.bindCustomRenderer("TabList", () -> {
            if (!globalSearchQuery.isEmpty()) {
                ClayComponents.tab("Tab_SearchResults", "Search Results", true, () -> {
                });
            } else {
                for (int i = 0; i < tabs.size(); i++) {
                    IMenuTab tab = tabs.get(i);
                    ClayComponents.tab("Tab_" + i, tab.getName(), activeTab == tab, () -> {
                        activeTab = tab;
                        categorySearchQuery = "";
                    });
                }
            }
        });

        xmlContext.bindCustomRenderer("ActiveContent", this::drawMainContent);

        xmlContext.bindCustomRenderer("ResultList", () -> {
            boolean foundAny = false;
            for (ConfigCategoryData cat : ConfigManager.CATEGORIES) {
                for (ConfigElement el : cat.elements) {
                    if (ClayConfigUI.elementMatches(el, globalSearchQuery)) {
                        foundAny = true;
                        ClayConfigUI.drawConfigElement(el, globalSearchQuery);
                    }
                }
            }
            if (!foundAny) text(
                    "No results found for '"
                            + globalSearchQuery + "'",
                    txt().size(1).color(150, 150, 150, 255));

            el(decl().layout(layout().sizing(GROW, 0, FIXED, 15)), () -> {
            });
        });
    }

    public void loadConfigCategories() {
        for (ConfigCategoryData category : ConfigManager.CATEGORIES) {
            boolean hasVisibleElements = false;
            for (ConfigElement el : category.elements) {
                if (!el.hidden) {
                    hasVisibleElements = true;
                    break;
                }
            }
            if (hasVisibleElements) addTab(new ConfigCategoryTab(category));
        }
    }

    @Override
    protected void buildLayout(int mouseX, int mouseY, float deltaTime) {
        float winWidth = Math.min(650, this.width - 20);
        float winHeight = Math.min(400, this.height - 20);
        this.currentContentHeight = winHeight - 40;

        el(decl()
                .id("ScreenRoot")
                .layout(layout()
                        .sizing(GROW, 0, GROW, 0)
                        .align(LayoutAlignmentX.CENTER, LayoutAlignmentY.CENTER)),
                () -> el(decl().layout(layout().sizing(FIXED, winWidth, FIXED, winHeight)),
                        () -> {
                    if (xmlRoot != null) xmlRoot.render(xmlContext);
                }));

        onPostBuildLayout();
    }

    protected void onPostBuildLayout() {
    }

    private void drawMainContent() {
        if (!globalSearchQuery.isEmpty()) {
            if (searchResultsXml != null) searchResultsXml.render(xmlContext);
        } else if (activeTab != null) {
            activeTab.draw();
        }
    }

    public class ConfigCategoryTab implements IMenuTab {
        public final ConfigCategoryData category;
        private final XmlNode tabXml;
        private final XmlContext ctx;

        public ConfigCategoryTab(ConfigCategoryData category) {
            this.category = category;
            this.tabXml = ClayXmlEngine.load("ui/category_tab.xml");
            this.ctx = new XmlContext();

            ctx.bindString("categoryName", () -> category.name);
            ctx.bindString("wrapperId", () -> "ScrollWrapper_" + category.id);
            ctx.bindString("scrollId", () -> "Scroll_" + category.id);
            ctx.bindString("scrollHeight", () -> String.valueOf(currentContentHeight - 40));

            ctx.bindInput("categorySearchQuery", () -> categorySearchQuery, val -> categorySearchQuery = val);

            ctx.bindCustomRenderer("ElementList", () -> {
                boolean foundAny = false;
                for (ConfigElement element : category.elements) {
                    if (ClayConfigUI.elementMatches(element, categorySearchQuery)) {
                        foundAny = true;
                        ClayConfigUI.drawConfigElement(element, categorySearchQuery);
                    }
                }
                if (!foundAny) text(
                        "No results found in this category.",
                        txt().size(1).color(150, 150, 150, 255)
                );

                el(decl().layout(layout().sizing(GROW, 0, FIXED, 15)), () -> {});
            });
        }

        @Override
        public String getName() {
            return category.name;
        }

        @Override
        public void draw() {
            if (tabXml != null) tabXml.render(ctx);
        }
    }
}