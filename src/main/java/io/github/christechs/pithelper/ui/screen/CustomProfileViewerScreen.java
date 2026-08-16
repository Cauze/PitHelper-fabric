/*
 * PitHelper - A Hypixel The Pit Helper Mod.
 * Copyright (C) 2026 Christian Steenkamp
 */

package io.github.christechs.pithelper.ui.screen;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import com.google.gson.JsonArray;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;

import io.github.christechs.clayj.enums.LayoutAlignmentY;
import io.github.christechs.pithelper.api.PitHelperAPI;
import io.github.christechs.pithelper.config.PitConfig;
import io.github.christechs.pithelper.ui.base.ClayScreen;
import io.github.christechs.pithelper.ui.components.ClayComponents;
import io.github.christechs.pithelper.ui.utils.PitUIUtil;
import io.github.christechs.pithelper.ui.xml.ClayXmlEngine;
import io.github.christechs.pithelper.ui.xml.XmlContext;
import io.github.christechs.pithelper.ui.xml.nodes.XmlNode;
import io.github.christechs.pithelper.utils.PitItemUtil;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.world.item.ItemStack;

import static io.github.christechs.clayj.ClayJ.decl;
import static io.github.christechs.clayj.ClayJ.el;
import static io.github.christechs.clayj.ClayJ.layout;
import static io.github.christechs.clayj.ClayJ.text;
import static io.github.christechs.clayj.ClayJ.txt;
import static io.github.christechs.clayj.enums.LayoutAlignmentX.CENTER;
import static io.github.christechs.clayj.enums.LayoutAlignmentX.LEFT;
import static io.github.christechs.clayj.enums.LayoutDirection.LEFT_TO_RIGHT;
import static io.github.christechs.clayj.enums.LayoutDirection.TOP_TO_BOTTOM;
import static io.github.christechs.clayj.enums.SizingType.FIXED;
import static io.github.christechs.clayj.enums.SizingType.FIT;
import static io.github.christechs.clayj.enums.SizingType.GROW;

public class CustomProfileViewerScreen extends ClayScreen {

	private final Screen parent;
	private final String targetUuid;
	private final Map<String, ItemStack[]> parsedInventories = new HashMap<>();
	public Screen screenToOpen = null;
	private float currentContentHeight = 0;

	private String currentTab = "main";
	private String currentInvTab = "inv_contents";
	private String statusMessage = "Fetching data...";

	private String inventorySearchQuery = "";
	private String invSearchEnchant = "";
	private String invSearchMinLives = "";
	private String invSearchMaxLives = "";
	private String invSearchType = "";
	private String lastFilterStateHash = "";
	private ItemStack[] cachedFilteredInventory = new ItemStack[0];
	private int inventoryPage = 0;

	private JsonObject mainData = null;
	private JsonObject statsData = null;
	private JsonObject profileData = null;
	private JsonObject rawInventories = null;

	private XmlNode xmlRoot;
	private XmlContext xmlContext;

	public CustomProfileViewerScreen(Screen parent, String targetUuid) {
		this.parent = parent;
		this.targetUuid = targetUuid;
		fetchData("main");
	}

	@Override
	protected void init() {
		super.init();
		xmlRoot = ClayXmlEngine.load("ui/profile_viewer.xml");
		xmlContext = new XmlContext();

		xmlContext.bindString("profileTitle", () -> {
			String name = mainData != null && mainData.has("name") ? mainData.get("name").getAsString() : targetUuid;
			return "§e" + name + "§7's Profile";
		});

		xmlContext.bindString("scrollHeight", () -> String.valueOf(currentContentHeight));
		xmlContext.bindBoolean("isTabMain", () -> currentTab.equals("main"));
		xmlContext.bindBoolean("isTabStats", () -> currentTab.equals("stats"));
		xmlContext.bindBoolean("isTabProfile", () -> currentTab.equals("profile"));
		xmlContext.bindBoolean("isTabInv", () -> currentTab.equals("inventories"));
		xmlContext.bindString("scrollId", () -> "Scroll_" + currentTab);

		xmlContext.bindAction("closeScreen", () -> this.screenToOpen = parent);
		xmlContext.bindAction("setTabMain", () -> {
			currentTab = "main";
			fetchData("main");
		});
		xmlContext.bindAction("setTabStats", () -> {
			currentTab = "stats";
			fetchData("stats");
		});
		xmlContext.bindAction("setTabProfile", () -> {
			currentTab = "profile";
			fetchData("profile");
		});
		xmlContext.bindAction("setTabInv", () -> {
			currentTab = "inventories";
			fetchData("inventories");
		});

		xmlContext.bindCustomRenderer("DynamicContent", () -> {
			if (currentTab.equals("main")) {
				drawMainTab();
			} else if (currentTab.equals("stats")) {
				drawStatsTab();
			} else if (currentTab.equals("profile")) {
				drawProfileTab();
			} else if (currentTab.equals("inventories")) {
				drawInventoriesTab();
			}
			el(decl().layout(layout().sizing(GROW, 0, FIXED, 15)), () -> {
			});
		});
	}

	private void fetchData(String tab) {
		statusMessage = "§eFetching " + tab + " data (May Queue)...";
		String key = PitConfig.api().pitHelperApiKey;

		if (tab.equals("main") && mainData == null) {
			PitHelperAPI.getPlayerMain(key, targetUuid).whenComplete((res, err) -> {
				if (err == null && res != null) {
					mainData = res.getAsJsonObject("data");
					statusMessage = "§aSuccess";
				} else if (err != null) {
					statusMessage = "§c" + err.getMessage();
				}
			});
		} else if (tab.equals("stats") && statsData == null) {
			PitHelperAPI.getPlayerStats(key, targetUuid).whenComplete((res, err) -> {
				if (err == null) {
					statsData = res.getAsJsonObject("data");
					statusMessage = "§aSuccess";
				} else {
					statusMessage = "§c" + err.getMessage();
				}
			});
		} else if (tab.equals("profile") && profileData == null) {
			PitHelperAPI.getPlayerProfile(key, targetUuid).whenComplete((res, err) -> {
				if (err == null) {
					profileData = res.getAsJsonObject("data");
					statusMessage = "§aSuccess";
				} else {
					statusMessage = "§c" + err.getMessage();
				}
			});
		} else if (tab.equals("inventories") && rawInventories == null) {
			PitHelperAPI.getPlayerInventories(key, targetUuid).whenComplete((res, err) -> {
				if (err == null) {
					rawInventories = res.getAsJsonObject("data").getAsJsonObject("raw_nbt_base64");
					parsedInventories.clear();
					parsedInventories.putAll(PitItemUtil.parseBase64Inventories(rawInventories));
					if (!parsedInventories.containsKey(currentInvTab) && !parsedInventories.isEmpty()) {
						currentInvTab = parsedInventories.keySet().iterator().next();
					}
					lastFilterStateHash = "";
					statusMessage = "§aSuccess";
				} else {
					statusMessage = "§c" + err.getMessage();
				}
			});
		}
	}

	@Override
	protected void buildLayout(int mouseX, int mouseY, float deltaTime) {
		hoveredItemTooltip = ItemStack.EMPTY;
		float winWidth = Math.min(550, this.width - 40);
		float winHeight = Math.min(450, this.height - 40);
		this.currentContentHeight = winHeight - 110;

		el(decl().id("ProfileRoot").layout(layout().sizing(GROW, 0, GROW, 0).align(CENTER, LayoutAlignmentY.CENTER)), () -> {
			el(decl().layout(layout().sizing(FIXED, winWidth, FIXED, winHeight)), () -> {
				if (xmlRoot != null) {
					xmlRoot.render(xmlContext);
				}
			});
		});

		if (this.screenToOpen != null) {
			this.mc.gui.setScreen(this.screenToOpen);
			this.screenToOpen = null;
		}
	}

	private void statRow(String label, String value) {
		text("§7" + label + ": " + value, txt().size(1));
	}

	private void drawMainTab() {
		if (mainData == null) {
			text(statusMessage, txt().size(1).color(200, 200, 200, 255));
			return;
		}
		el(decl().bg(35, 38, 45, 255).radius(6).layout(layout().sizing(GROW, 0, FIT, 0).padding(15, 15).dir(TOP_TO_BOTTOM).gap(4)), () -> {
			statRow("Level", "§b" + mainData.get("level").getAsString());
			statRow("Prestige", "§e" + mainData.get("prestige").getAsString());
			statRow("Renown", "§e" + mainData.get("renown").getAsString());
			statRow("Gold", "§6" + String.format("%,.2f", mainData.get("cash").getAsDouble()) + "g");
			statRow("Lifetime XP", "§b" + String.format("%,d", mainData.get("xp").getAsLong()));
		});
	}

	private void drawStatsTab() {
		if (statsData == null) {
			text(statusMessage, txt().size(1).color(200, 200, 200, 255));
			return;
		}
		el(decl().layout(layout().dir(LEFT_TO_RIGHT).sizing(GROW, 0, FIT, 0).gap(15)), () -> {
			el(decl().bg(35, 38, 45, 255).radius(6).layout(layout().sizing(GROW, 0, FIT, 0).padding(15, 15).dir(TOP_TO_BOTTOM).gap(4)), () -> {
				text("Combat", txt().size(1).color(255, 200, 50, 255));
				statRow("Kills", "§a" + String.format("%,d", statsData.get("kills").getAsLong()));
				statRow("Deaths", "§c" + String.format("%,d", statsData.get("deaths").getAsLong()));
				double kd = statsData.get("deaths").getAsLong() == 0 ? statsData.get("kills").getAsDouble() : statsData.get("kills").getAsDouble() / statsData.get("deaths").getAsDouble();
				statRow("K/D Ratio", "§e" + String.format("%.2f", kd));
			});
			el(decl().bg(35, 38, 45, 255).radius(6).layout(layout().sizing(GROW, 0, FIT, 0).padding(15, 15).dir(TOP_TO_BOTTOM).gap(4)), () -> {
				text("Damage", txt().size(1).color(255, 200, 50, 255));
				statRow("Damage Dealt", "§c" + String.format("%,d", statsData.get("damage_dealt").getAsLong()));
				statRow("Damage Received", "§c" + String.format("%,d", statsData.get("damage_received").getAsLong()));
			});
		});
	}

	private void drawProfileTab() {
		if (profileData == null) {
			text(statusMessage, txt().size(1).color(200, 200, 200, 255));
			return;
		}
		el(decl().layout(layout().dir(LEFT_TO_RIGHT).sizing(GROW, 0, FIT, 0).gap(15)), () -> {
			el(decl().bg(35, 38, 45, 255).radius(6).layout(layout().sizing(GROW, 0, FIT, 0).padding(15, 15).dir(TOP_TO_BOTTOM).gap(4)), () -> {
				text("Equipped Perks", txt().size(1).color(255, 200, 50, 255));
				JsonArray perks = JsonParser.parseString(profileData.get("perks_json").getAsString()).getAsJsonArray();
				for (int i = 0; i < 4; i++) {
					statRow("Slot " + (i + 1), "§a" + (i < perks.size() ? perks.get(i).getAsString() : "Empty"));
				}
			});
			el(decl().bg(35, 38, 45, 255).radius(6).layout(layout().sizing(GROW, 0, FIT, 0).padding(15, 15).dir(TOP_TO_BOTTOM).gap(4)), () -> {
				text("Killstreaks", txt().size(1).color(255, 200, 50, 255));
				JsonArray streaks = JsonParser.parseString(profileData.get("killstreaks_json").getAsString()).getAsJsonArray();
				for (int i = 0; i < 4; i++) {
					statRow("Slot " + (i + 1), "§e" + (i < streaks.size() ? streaks.get(i).getAsString() : "Empty"));
				}
			});
		});
	}

	private void drawInventoriesTab() {
		if (rawInventories == null) {
			text(statusMessage, txt().size(1).color(200, 200, 200, 255));
			return;
		}

		el(decl().layout(layout().dir(TOP_TO_BOTTOM).gap(10).sizing(GROW, 0, FIT, 0)), () -> {
			el(decl().layout(layout().dir(LEFT_TO_RIGHT).gap(8).align(LEFT, LayoutAlignmentY.CENTER).sizing(GROW, 0, FIT, 0)), () -> {
				if (parsedInventories.containsKey("inv_contents")) {
					ClayComponents.button("SubTab_MainInv", "Main", () -> {
						currentInvTab = "inv_contents";
						inventoryPage = 0;
					});
				}
				if (parsedInventories.containsKey("inv_armor")) {
					ClayComponents.button("SubTab_Armor", "Armor", () -> {
						currentInvTab = "inv_armor";
						inventoryPage = 0;
					});
				}
				if (parsedInventories.containsKey("inv_enderchest")) {
					ClayComponents.button("SubTab_Echest", "Enderchest", () -> {
						currentInvTab = "inv_enderchest";
						inventoryPage = 0;
					});
				}
				if (parsedInventories.containsKey("item_stash")) {
					ClayComponents.button("SubTab_Stash", "Stash", () -> {
						currentInvTab = "item_stash";
						inventoryPage = 0;
					});
				}
				el(decl().layout(layout().sizing(GROW, 0, FIXED, 0)), () -> {
				});
				ClayComponents.textInput("InvSearchBox", "Filter Items...", inventorySearchQuery, 150, val -> inventorySearchQuery = val);
			});

			el(decl().bg(35, 38, 45, 255).radius(6).layout(layout().sizing(GROW, 0, FIT, 0).padding(10, 10).dir(LEFT_TO_RIGHT).gap(15).align(LEFT, LayoutAlignmentY.CENTER)), () -> {
				el(decl().layout(layout().dir(TOP_TO_BOTTOM).gap(4)), () -> {
					text("Enchant", txt().size(1).color(180, 180, 180, 255));
					ClayComponents.textInput("InvFilterEnchant", "e.g. Shark", invSearchEnchant, 100, val -> invSearchEnchant = val);
				});
				el(decl().layout(layout().dir(TOP_TO_BOTTOM).gap(4)), () -> {
					text("Type", txt().size(1).color(180, 180, 180, 255));
					ClayComponents.textInput("InvFilterType", "Sword, Pants", invSearchType, 80, val -> invSearchType = val);
				});
				el(decl().layout(layout().dir(TOP_TO_BOTTOM).gap(4)), () -> {
					text("Min Lives", txt().size(1).color(180, 180, 180, 255));
					ClayComponents.textInput("InvFilterMinLives", "Any", invSearchMinLives, 50, val -> invSearchMinLives = val);
				});
				el(decl().layout(layout().dir(TOP_TO_BOTTOM).gap(4)), () -> {
					text("Max Lives", txt().size(1).color(180, 180, 180, 255));
					ClayComponents.textInput("InvFilterMaxLives", "Any", invSearchMaxLives, 50, val -> invSearchMaxLives = val);
				});
			});
		});

		ItemStack[] items = parsedInventories.get(currentInvTab);
		if (items != null) {
			String currentState = currentInvTab + "|" + inventorySearchQuery + "|" + invSearchEnchant + "|" + invSearchType + "|" + invSearchMinLives + "|" + invSearchMaxLives;
			if (!currentState.equals(lastFilterStateHash)) {
				List<ItemStack> filtered = new ArrayList<>();
				boolean noFilters = inventorySearchQuery.isEmpty() && invSearchEnchant.isEmpty() && invSearchType.isEmpty() && invSearchMinLives.isEmpty() && invSearchMaxLives.isEmpty();
				for (ItemStack stack : items) {
					if (stack == null || stack.isEmpty()) {
						if (noFilters) {
							filtered.add(null);
						}
					} else if (PitItemUtil.itemMatchesFilters(stack, inventorySearchQuery, invSearchEnchant, invSearchType, PitItemUtil.parseSafeInt(invSearchMinLives, -1), PitItemUtil.parseSafeInt(invSearchMaxLives, -1))) {
						filtered.add(stack);
					} else if (noFilters) {
						filtered.add(stack);
					}
				}
				cachedFilteredInventory = filtered.toArray(new ItemStack[0]);
				inventoryPage = 0;
				lastFilterStateHash = currentState;
			}

			el(decl().bg(30, 32, 38, 255).radius(6).layout(layout().sizing(FIT, 0, FIT, 0).padding(10, 10).dir(TOP_TO_BOTTOM).gap(4)), () -> {
				int itemsPerPage = 36;
				int totalItems = cachedFilteredInventory.length;
				int totalPages = (int) Math.ceil((double) totalItems / itemsPerPage);
				if (inventoryPage >= totalPages && totalPages > 0) {
					inventoryPage = totalPages - 1;
				}
				PitUIUtil.drawItemGrid(cachedFilteredInventory, inventoryPage, itemsPerPage, 9, "Slot_" + currentInvTab + "_", stack -> hoveredItemTooltip = stack, null);
				PitUIUtil.drawPageNav(inventoryPage, totalPages, "InvPage", () -> inventoryPage--, () -> inventoryPage++);
			});
		}
	}
}
