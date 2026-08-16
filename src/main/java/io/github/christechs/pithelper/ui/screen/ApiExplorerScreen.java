/*
 * PitHelper - A Hypixel The Pit Helper Mod.
 * Copyright (C) 2026 Christian Steenkamp
 */

package io.github.christechs.pithelper.ui.screen;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.Map;

import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;

import io.github.christechs.clayj.enums.LayoutAlignmentY;
import io.github.christechs.pithelper.api.PitHelperAPI;
import io.github.christechs.pithelper.config.PitConfig;
import io.github.christechs.pithelper.ui.base.ClayScreen;
import io.github.christechs.pithelper.ui.components.ClayComponents;
import io.github.christechs.pithelper.ui.utils.PitUIUtil;
import io.github.christechs.pithelper.ui.xml.ClayXmlEngine;
import io.github.christechs.pithelper.ui.xml.XmlContext;
import io.github.christechs.pithelper.ui.xml.nodes.XmlNode;
import io.github.christechs.pithelper.utils.EnchantDictionary;
import io.github.christechs.pithelper.utils.PitItemUtil;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.client.multiplayer.PlayerInfo;
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

public class ApiExplorerScreen extends ClayScreen {

	private static final List<LobbyPlayer> lobbyPlayers = new ArrayList<>();
	private static List<MatchedLobbyItem> cachedLobbyMatches = null;
	private final Screen parent;
	public Screen screenToOpen = null;
	private float currentContentHeight = 0;
	private XmlNode xmlRoot;
	private XmlContext xmlContext;
	private String currentTab = "player";

	private String playerSearchQuery = "";
	private String playerSearchStatus = "Idle";
	private JsonObject lastPlayerMain = null;

	private String itemSearchEnchant = "";
	private String itemSearchMinLvl = "1";
	private String itemSearchMinTier = "0";
	private String itemSearchMinLives = "";
	private String itemSearchMaxLives = "";
	private String itemSearchType = "";
	private String itemSearchStatus = "Idle";

	private String currentLobbySubTab = "players";
	private String lobbySearchEnchant = "";
	private String lobbySearchType = "";
	private String lobbySearchMinLives = "";
	private String lobbySearchMaxLives = "";

	private int lobbyPlayerPage = 0;
	private int lobbyItemPage = 0;
	private List<ItemStack> cachedItemStacks = new ArrayList<>();
	private List<String> cachedOwnerUuids = new ArrayList<>();
	private int currentPage = 0;

	public ApiExplorerScreen(Screen parent) {
		this.parent = parent;
	}

	@Override
	protected void init() {
		super.init();
		xmlRoot = ClayXmlEngine.load("ui/api_explorer.xml");
		xmlContext = new XmlContext();

		xmlContext.bindString("scrollHeight", () -> String.valueOf(currentContentHeight));
		xmlContext.bindBoolean("isTabPlayer", () -> currentTab.equals("player"));
		xmlContext.bindBoolean("isTabItem", () -> currentTab.equals("item"));
		xmlContext.bindBoolean("isTabLobby", () -> currentTab.equals("lobby"));
		xmlContext.bindString("scrollId", () -> "Scroll_" + currentTab);

		xmlContext.bindAction("closeScreen", () -> this.screenToOpen = parent);
		xmlContext.bindAction("setTabPlayer", () -> currentTab = "player");
		xmlContext.bindAction("setTabItem", () -> currentTab = "item");
		xmlContext.bindAction("setTabLobby", () -> {
			currentTab = "lobby";
			if (lobbyPlayers.isEmpty()) {
				initializeLobbyPlayers();
			}
		});

		xmlContext.bindCustomRenderer("DynamicContent", () -> {
			if (currentTab.equals("player")) {
				drawPlayerTab();
			} else if (currentTab.equals("item")) {
				drawItemTab();
			} else {
				drawLobbyTab();
			}
			el(decl().layout(layout().sizing(GROW, 0, FIXED, 15)), () -> {
			});
		});
	}

	@Override
	protected void buildLayout(int mouseX, int mouseY, float deltaTime) {
		hoveredItemTooltip = ItemStack.EMPTY;
		float winWidth = Math.min(650, this.width - 20);
		float winHeight = Math.min(450, this.height - 20);
		this.currentContentHeight = winHeight - 110;

		el(decl().id("ExplorerRoot").layout(layout().sizing(GROW, 0, GROW, 0).align(CENTER, LayoutAlignmentY.CENTER)), () -> {
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

	private void drawPlayerTab() {
		el(decl().bg(35, 38, 45, 255).radius(6).layout(layout().sizing(GROW, 0, FIT, 0).padding(15, 15).dir(TOP_TO_BOTTOM).gap(10)), () -> {
			text("Look up a specific player", txt().size(1).color(50, 200, 255, 255));
			el(decl().layout(layout().dir(LEFT_TO_RIGHT).gap(10).align(LEFT, LayoutAlignmentY.CENTER)), () -> {
				ClayComponents.textInput("PlayerSearchInput", "Username/UUID...", playerSearchQuery, 200, val -> playerSearchQuery = val);
				ClayComponents.button("Btn_SearchPlayer", "Search", this::executePlayerSearch);
			});

			if (lastPlayerMain != null) {
				String name = lastPlayerMain.has("name") ? lastPlayerMain.get("name").getAsString() : playerSearchQuery;
				el(decl().bg(24, 26, 30, 255).radius(4).layout(layout().sizing(GROW, 0, FIT, 0).padding(10, 10).dir(LEFT_TO_RIGHT).gap(15).align(LEFT, LayoutAlignmentY.CENTER)), () -> {
					el(decl().layout(layout().dir(TOP_TO_BOTTOM).gap(4)), () -> {
						text("Player: §e" + name, txt().size(1).color(255, 255, 255, 255));
						text("Prestige: §e" + lastPlayerMain.get("prestige").getAsInt() + " §7| Level: §b" + lastPlayerMain.get("level").getAsInt(), txt().size(1).color(200, 200, 200, 255));
					});
					ClayComponents.button("Btn_ViewProfile", "Open Profile Viewer", () -> this.screenToOpen = new CustomProfileViewerScreen(this, playerSearchQuery));
				});
			} else {
				text("Status: " + playerSearchStatus, txt().size(1).color(180, 180, 180, 255));
			}
		});
	}

	private void drawItemTab() {
		el(decl().bg(35, 38, 45, 255).radius(6).layout(layout().sizing(GROW, 0, FIT, 0).padding(15, 15).dir(TOP_TO_BOTTOM).gap(15)), () -> {
			text("Global Mystic Item Search (Cost: 2 Tokens)", txt().size(1).color(50, 255, 100, 255));

			el(decl().layout(layout().dir(TOP_TO_BOTTOM).gap(10)), () -> {
				el(decl().layout(layout().dir(LEFT_TO_RIGHT).gap(15).align(LEFT, LayoutAlignmentY.CENTER)), () -> {
					el(decl().layout(layout().dir(TOP_TO_BOTTOM).gap(4)), () -> {
						text("Enchant Name", txt().size(1).color(180, 180, 180, 255));
						ClayComponents.textInput("ItemSearch_Enchant", "e.g. Strike Gold", itemSearchEnchant, 150, val -> itemSearchEnchant = val);
					});
					el(decl().layout(layout().dir(TOP_TO_BOTTOM).gap(4)), () -> {
						text("Min Lvl", txt().size(1).color(180, 180, 180, 255));
						ClayComponents.textInput("ItemSearch_MinLvl", "1", itemSearchMinLvl, 50, val -> itemSearchMinLvl = val);
					});
					el(decl().layout(layout().dir(TOP_TO_BOTTOM).gap(4)), () -> {
						text("Min Tier", txt().size(1).color(180, 180, 180, 255));
						ClayComponents.textInput("ItemSearch_MinTier", "0", itemSearchMinTier, 50, val -> itemSearchMinTier = val);
					});
				});

				el(decl().layout(layout().dir(LEFT_TO_RIGHT).gap(15).align(LEFT, LayoutAlignmentY.CENTER)), () -> {
					el(decl().layout(layout().dir(TOP_TO_BOTTOM).gap(4)), () -> {
						text("Item Type", txt().size(1).color(180, 180, 180, 255));
						ClayComponents.textInput("ItemSearch_Type", "Sword, Bow, Pants...", itemSearchType, 150, val -> itemSearchType = val);
					});
					el(decl().layout(layout().dir(TOP_TO_BOTTOM).gap(4)), () -> {
						text("Min Lives", txt().size(1).color(180, 180, 180, 255));
						ClayComponents.textInput("ItemSearch_MinLives", "Any", itemSearchMinLives, 50, val -> itemSearchMinLives = val);
					});
					el(decl().layout(layout().dir(TOP_TO_BOTTOM).gap(4)), () -> {
						text("Max Lives", txt().size(1).color(180, 180, 180, 255));
						ClayComponents.textInput("ItemSearch_MaxLives", "Any", itemSearchMaxLives, 50, val -> itemSearchMaxLives = val);
					});
					el(decl().layout(layout().dir(TOP_TO_BOTTOM).gap(4)), () -> {
						el(decl().layout(layout().sizing(FIXED, 0, FIXED, 10)), () -> {
						});
						ClayComponents.button("Btn_SearchItems", "Search DB", this::executeItemSearch);
					});
				});
			});

			if (!cachedItemStacks.isEmpty() || itemSearchStatus.contains("Search Complete")) {
				int totalItems = cachedItemStacks.size();
				int itemsPerPage = 27;
				int totalPages = (int) Math.ceil(totalItems / (double) itemsPerPage);
				if (currentPage >= totalPages && totalPages > 0) {
					currentPage = totalPages - 1;
				}

				text("Found " + totalItems + " matches:", txt().size(1).color(255, 255, 255, 255));
				el(decl().bg(24, 26, 30, 255).radius(4).layout(layout().sizing(GROW, 0, FIT, 0).padding(10, 10).dir(TOP_TO_BOTTOM).gap(10)), () -> {
					if (totalItems == 0) {
						text("No items found matching criteria.", txt().size(1).color(150, 150, 150, 255));
					} else {
						PitUIUtil.drawItemGrid(cachedItemStacks.toArray(new ItemStack[0]), currentPage, itemsPerPage, 9, "GlobalSlot_",
							stack -> hoveredItemTooltip = stack,
							idx -> this.screenToOpen = new CustomProfileViewerScreen(this, cachedOwnerUuids.get(idx)));
						PitUIUtil.drawPageNav(currentPage, totalPages, "GlobalPage", () -> currentPage--, () -> currentPage++);
					}
				});
			} else {
				text("Status: " + itemSearchStatus, txt().size(1).color(180, 180, 180, 255));
			}
		});
	}

	private void drawLobbyTab() {
		el(decl().bg(35, 38, 45, 255).radius(6).layout(layout().sizing(GROW, 0, FIT, 0).padding(15, 15).dir(TOP_TO_BOTTOM).gap(10)), () -> {
			text("Lobby Player Explorer", txt().size(1).color(50, 200, 255, 255));

			el(decl().layout(layout().dir(LEFT_TO_RIGHT).gap(10).align(LEFT, LayoutAlignmentY.CENTER)), () -> {
				ClayComponents.button("Btn_InitLobby", "Refresh Lobby List", this::initializeLobbyPlayers);
				ClayComponents.button("Btn_LoadLobby", "Load All Pending", this::fetchAllPendingLobby);
				int loaded = (int) lobbyPlayers.stream().filter(p -> p.status.equals("§aLoaded")).count();
				text("Players: " + lobbyPlayers.size() + " | Loaded: " + loaded, txt().size(1).color(200, 200, 200, 255));
			});

			el(decl().layout(layout().dir(LEFT_TO_RIGHT).gap(8).sizing(GROW, 0, FIT, 0).padding(0, 10)), () -> {
				ClayComponents.tab("SubTab_LobbyPlayers", "Players", currentLobbySubTab.equals("players"), () -> currentLobbySubTab = "players");
				ClayComponents.tab("SubTab_LobbyItems", "Item Search", currentLobbySubTab.equals("items"), () -> currentLobbySubTab = "items");
			});

			if (currentLobbySubTab.equals("players")) {
				el(decl().layout(layout().dir(TOP_TO_BOTTOM).gap(4)), () -> {
					int totalPlayers = lobbyPlayers.size();
					if (totalPlayers == 0) {
						text("No players found. Click Refresh Lobby List.", txt().size(1).color(150, 150, 150, 255));
					} else {
						int playersPerPage = 5;
						int totalPages = (int) Math.ceil((double) totalPlayers / playersPerPage);
						if (lobbyPlayerPage >= totalPages && totalPages > 0) {
							lobbyPlayerPage = totalPages - 1;
						}
						int startIdx = lobbyPlayerPage * playersPerPage;
						int endIdx = Math.min(startIdx + playersPerPage, totalPlayers);
						for (int i = startIdx; i < endIdx; i++) {
							LobbyPlayer lp = lobbyPlayers.get(i);
							int relativeI = i - startIdx;
							el(decl().bg(24, 26, 30, 255).radius(4).layout(layout().sizing(GROW, 0, FIT, 0).padding(10, 10).dir(LEFT_TO_RIGHT).gap(15).align(LEFT, LayoutAlignmentY.CENTER)), () -> {
								text("§e" + lp.name, txt().size(1).color(255, 255, 255, 255));
								text("Status: " + lp.status, txt().size(1).color(200, 200, 200, 255));
								el(decl().layout(layout().sizing(GROW, 0, FIXED, 0)), () -> {
								});
								ClayComponents.button("Btn_Reload_" + relativeI, "Reload", () -> fetchLobbyPlayer(lp));
								if (lp.status.equals("§aLoaded")) {
									ClayComponents.button("Btn_View_" + relativeI, "View Profile", () -> this.screenToOpen = new CustomProfileViewerScreen(this, lp.name));
								}
							});
						}
						PitUIUtil.drawPageNav(lobbyPlayerPage, totalPages, "LobbyPlayerPage", () -> lobbyPlayerPage--, () -> lobbyPlayerPage++);
					}
				});
			} else {
				el(decl().layout(layout().dir(TOP_TO_BOTTOM).gap(10)), () -> {
					el(decl().layout(layout().dir(LEFT_TO_RIGHT).gap(15).align(LEFT, LayoutAlignmentY.CENTER)), () -> {
						el(decl().layout(layout().dir(TOP_TO_BOTTOM).gap(4)), () -> {
							text("Keyword", txt().size(1).color(180, 180, 180, 255));
							ClayComponents.textInput("LobbySearch_Enchant", "e.g. Strike Gold", lobbySearchEnchant, 150, val -> {
								lobbySearchEnchant = val;
								cachedLobbyMatches = null;
								lobbyItemPage = 0;
							});
						});
						el(decl().layout(layout().dir(TOP_TO_BOTTOM).gap(4)), () -> {
							text("Item Type", txt().size(1).color(180, 180, 180, 255));
							ClayComponents.textInput("LobbySearch_Type", "Sword, Pants...", lobbySearchType, 100, val -> {
								lobbySearchType = val;
								cachedLobbyMatches = null;
								lobbyItemPage = 0;
							});
						});
						el(decl().layout(layout().dir(TOP_TO_BOTTOM).gap(4)), () -> {
							text("Min Lives", txt().size(1).color(180, 180, 180, 255));
							ClayComponents.textInput("LobbySearch_MinLives", "Any", lobbySearchMinLives, 50, val -> {
								lobbySearchMinLives = val;
								cachedLobbyMatches = null;
								lobbyItemPage = 0;
							});
						});
						el(decl().layout(layout().dir(TOP_TO_BOTTOM).gap(4)), () -> {
							text("Max Lives", txt().size(1).color(180, 180, 180, 255));
							ClayComponents.textInput("LobbySearch_MaxLives", "Any", lobbySearchMaxLives, 50, val -> {
								lobbySearchMaxLives = val;
								cachedLobbyMatches = null;
								lobbyItemPage = 0;
							});
						});
					});

					if (cachedLobbyMatches == null) {
						cachedLobbyMatches = getLobbyItemMatches();
					}
					int totalItems = cachedLobbyMatches.size();
					text("Found " + totalItems + " matches in loaded lobby profiles:", txt().size(1).color(255, 255, 255, 255));
					el(decl().bg(24, 26, 30, 255).radius(4).layout(layout().sizing(GROW, 0, FIT, 0).padding(10, 10).dir(TOP_TO_BOTTOM).gap(10)), () -> {
						if (totalItems == 0) {
							text("No items found matching criteria.", txt().size(1).color(150, 150, 150, 255));
						} else {
							int itemsPerPage = 27;
							int totalPages = (int) Math.ceil((double) totalItems / itemsPerPage);
							if (lobbyItemPage >= totalPages && totalPages > 0) {
								lobbyItemPage = totalPages - 1;
							}
							ItemStack[] arr = cachedLobbyMatches.stream().map(m -> m.stack).toArray(ItemStack[]::new);
							PitUIUtil.drawItemGrid(arr, lobbyItemPage, itemsPerPage, 9, "LobbySlot_",
								stack -> hoveredItemTooltip = stack,
								idx -> this.screenToOpen = new CustomProfileViewerScreen(this, cachedLobbyMatches.get(idx).ownerName));
							PitUIUtil.drawPageNav(lobbyItemPage, totalPages, "LobbyItemPage", () -> lobbyItemPage--, () -> lobbyItemPage++);
						}
					});
				});
			}
		});
	}

	private void initializeLobbyPlayers() {
		lobbyPlayers.clear();
		cachedLobbyMatches = null;
		lobbyPlayerPage = 0;
		lobbyItemPage = 0;
		if (Minecraft.getInstance().getConnection() == null) {
			return;
		}
		Collection<PlayerInfo> players = Minecraft.getInstance().getConnection().getListedOnlinePlayers();
		for (PlayerInfo info : players) {
			LobbyPlayer lp = new LobbyPlayer();
			lp.name = info.getProfile().name();
			lp.uuid = info.getProfile().id().toString().replace("-", "");
			lobbyPlayers.add(lp);
		}
	}

	private void fetchLobbyPlayer(LobbyPlayer lp) {
		lp.status = "§eLoading...";
		PitHelperAPI.getPlayerInventories(PitConfig.api().pitHelperApiKey, lp.uuid).whenComplete((res, err) -> {
			if (err != null) {
				lp.status = "§cFailed";
			} else {
				try {
					JsonObject rawInv = res.getAsJsonObject("data").getAsJsonObject("raw_nbt_base64");
					lp.inventories = PitItemUtil.parseBase64Inventories(rawInv);
					lp.status = "§aLoaded";
					cachedLobbyMatches = null;
				} catch (Exception e) {
					lp.status = "§cError";
				}
			}
		});
	}

	private void fetchAllPendingLobby() {
		for (LobbyPlayer lp : lobbyPlayers) {
			if (lp.status.equals("Pending") || lp.status.equals("§cFailed") || lp.status.equals("§cError")) {
				fetchLobbyPlayer(lp);
			}
		}
	}

	private List<MatchedLobbyItem> getLobbyItemMatches() {
		List<MatchedLobbyItem> list = new ArrayList<>();
		for (LobbyPlayer lp : lobbyPlayers) {
			if (lp.status.equals("§aLoaded") && lp.inventories != null) {
				for (ItemStack[] inv : lp.inventories.values()) {
					for (ItemStack stack : inv) {
						if (PitItemUtil.itemMatchesFilters(stack, "", lobbySearchEnchant, lobbySearchType, PitItemUtil.parseSafeInt(lobbySearchMinLives, -1), PitItemUtil.parseSafeInt(lobbySearchMaxLives, -1))) {
							list.add(new MatchedLobbyItem(PitItemUtil.withOwnerLore(stack, lp.name), lp.name));
						}
					}
				}
			}
		}
		return list;
	}

	private void executePlayerSearch() {
		if (playerSearchQuery.isEmpty()) {
			return;
		}
		playerSearchStatus = "§eContacting Server (May Queue)...";
		lastPlayerMain = null;
		PitHelperAPI.getPlayerMain(PitConfig.api().pitHelperApiKey, playerSearchQuery).whenComplete((res, err) -> {
			if (err == null) {
				lastPlayerMain = res.getAsJsonObject("data");
				playerSearchStatus = "§aSuccess!";
			} else {
				playerSearchStatus = "§c" + err.getMessage();
			}
		});
	}

	private void executeItemSearch() {
		if (itemSearchEnchant.isEmpty()) {
			return;
		}
		itemSearchStatus = "§eSearching Database...";
		currentPage = 0;
		String internalEnchant = EnchantDictionary.getInternalName(itemSearchEnchant);
		int minLvl = PitItemUtil.parseSafeInt(itemSearchMinLvl, 1);
		int minTier = PitItemUtil.parseSafeInt(itemSearchMinTier, 0);

		PitHelperAPI.searchItems(PitConfig.api().pitHelperApiKey, internalEnchant, minLvl, minTier).whenComplete((res, err) -> {
			if (err == null) {
				JsonArray rawMatches = res.getAsJsonArray("matches");
				List<ItemStack> newCachedItems = new ArrayList<>();
				List<String> newCachedUuids = new ArrayList<>();
				int filterMinLives = PitItemUtil.parseSafeInt(itemSearchMinLives, -1);
				int filterMaxLives = PitItemUtil.parseSafeInt(itemSearchMaxLives, -1);
				String filterType = itemSearchType.toLowerCase().trim();

				for (JsonElement element : rawMatches) {
					JsonObject item = element.getAsJsonObject();
					boolean matches = true;
					if (filterMinLives != -1 || filterMaxLives != -1) {
						if (!item.has("lives") || item.get("lives").isJsonNull()) {
							matches = false;
						} else {
							int itemLives = item.get("lives").getAsInt();
							if (filterMinLives != -1 && itemLives < filterMinLives) {
								matches = false;
							}
							if (filterMaxLives != -1 && itemLives > filterMaxLives) {
								matches = false;
							}
						}
					}
					if (!filterType.isEmpty()) {
						int itemId = item.has("item_id") && !item.get("item_id").isJsonNull() ? item.get("item_id").getAsInt() : -1;
						if (filterType.contains("sword") && itemId != 283) {
							matches = false;
						} else if (filterType.contains("bow") && itemId != 261) {
							matches = false;
						} else if (filterType.contains("pant") && itemId != 300) {
							matches = false;
						}
					}
					if (matches) {
						newCachedItems.add(PitItemUtil.synthesizeItem(item));
						newCachedUuids.add(item.has("owner_uuid") ? item.get("owner_uuid").getAsString() : "");
					}
				}
				cachedItemStacks = newCachedItems;
				cachedOwnerUuids = newCachedUuids;
				itemSearchStatus = "§aSearch Complete!";
			} else {
				itemSearchStatus = "§c" + err.getMessage();
			}
		});
	}

	private static class LobbyPlayer {
		String name;
		String uuid;
		String status = "Pending";
		Map<String, ItemStack[]> inventories = null;
	}

	private static class MatchedLobbyItem {
		ItemStack stack;
		String ownerName;

		MatchedLobbyItem(ItemStack stack, String ownerName) {
			this.stack = stack;
			this.ownerName = ownerName;
		}
	}
}
