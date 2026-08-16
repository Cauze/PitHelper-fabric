/*
 * ClayJ is licensed under the zlib/libpng license.
 * Copyright (c) 2026 Christian Steenkamp
 *
 * This software is provided 'as-is', without any express or implied warranty.
 * In no event will the authors be held liable for any damages arising from the
 * use of this software.
 *
 * Permission is granted to anyone to use this software for any purpose,
 * including commercial applications, and to alter it and redistribute it
 * freely, subject to the following restrictions:
 *
 * 1. The origin of this software must not be misrepresented; you must not
 * claim that you wrote the original software. If you use this software in a
 * product, an acknowledgment in the product documentation would be
 * appreciated but is not required.
 * 2. Altered source versions must be plainly marked as such, and must not
 * be misrepresented as being the original software.
 * 3. This notice may not be removed or altered from any source distribution.
 */

package io.github.christechs.clayj;

import io.github.christechs.clayj.config.*;
import io.github.christechs.clayj.core.ClayJFunctions.ErrorHandler;
import io.github.christechs.clayj.core.ClayJFunctions.MeasureTextFunction;
import io.github.christechs.clayj.core.ClayJFunctions.QueryScrollOffsetFunction;
import io.github.christechs.clayj.core.*;
import io.github.christechs.clayj.enums.ClayJError;
import io.github.christechs.clayj.math.Dimensions;
import io.github.christechs.clayj.math.Vector2;
import io.github.christechs.clayj.util.ArenaPool;

import java.util.Arrays;

public final class ClayJContext {

    public static final int MAX_SCROLL_CONTAINERS = 32;
    public final int maxElementCount;
    public final int maxMeasureTextCacheWordCount;

    public final Vector2 scratchVector = new Vector2();
    public final Dimensions scratchDimensions = new Dimensions();

    public final MousePointerData pointerInfo = new MousePointerData();
    public final Dimensions layoutDimensions = new Dimensions();
    public final LayoutElement[] layoutElements;
    public final RenderCommand[] renderCommands;
    public final int[] openLayoutElementStack;
    public final int[] layoutElementChildrenBuffer;
    public final int[] layoutElementChildren;
    public final int[] openClipElementStack;
    public final int[] layoutElementClipElementIds;
    public final LayoutConfigBuilder[] layoutConfigs;
    public final SharedConfigBuilder[] sharedElementConfigs;
    public final BorderConfigBuilder[] borderElementConfigs;
    public final FloatingConfigBuilder[] floatingElementConfigs;
    public final ScrollConfigBuilder[] scrollElementConfigs;
    public final ImageConfigBuilder[] imageElementConfigs;
    public final TextConfigBuilder[] textElementConfigs;
    public final CustomConfigBuilder[] customElementConfigs;
    public final LayoutElementHashMapItem[] layoutElementHashMapItemPool;
    public final int[] layoutElementsHashBuckets;
    public final LayoutElementTreeRoot[] layoutElementTreeRoots;
    public final MeasureTextCacheItem[] measureTextHashMapInternal;
    public final int[] measureTextHashMapInternalFreeList;
    public final int[] measureTextHashMap;
    public final MeasuredWord[] measuredWords;
    public final int[] measuredWordsFreeList;
    public final TextElementData[] textElementData;
    public final WrappedTextLine[] wrappedTextLines;
    public final int[] imageElementPointers;
    public final ElementId[] pointerOverIds;
    public final ScrollContainerDataInternal[] scrollContainerDatas;
    public final LayoutElementTreeNode[] layoutElementTreeNodes;
    public final boolean[] treeNodeVisited;
    public final int[] resizableBuffer;

    public final ArenaPool<ElementDeclBuilder> transientDecls;
    public final ArenaPool<LayoutConfigBuilder> transientLayouts;
    public final ArenaPool<TextConfigBuilder> transientTexts;
    public final ArenaPool<CustomConfigBuilder> transientCustoms;
    public final ArenaPool<ElementId> transientIds;

    final ElementDeclBuilder rootDeclBuilder = new ElementDeclBuilder();
    public MeasureTextFunction measureTextFunction;
    public ErrorHandler errorHandler = (errorType) -> System.err.println("ClayJ Error [" + errorType + "]: " + errorType.getDefaultMessage());
    public QueryScrollOffsetFunction queryScrollOffsetFunction;
    public boolean externalScrollHandlingEnabled = false;

    public boolean maxElementsExceeded = false;
    public boolean maxRenderCommandsExceeded = false;
    public boolean maxTextMeasureCacheExceeded = false;
    public boolean textMeasurementFunctionNotSet = false;
    public int generation = 0;
    public int dynamicElementIndex = 0;
    public int layoutElementsLength = 0;
    public int renderCommandsLength = 0;
    public int openLayoutElementStackLength = 0;
    public int layoutElementChildrenBufferLength = 0;
    public int layoutElementChildrenLength = 0;
    public int openClipElementStackLength = 0;
    public int layoutConfigsLength = 0;
    public int sharedElementConfigsLength = 0;
    public int borderElementConfigsLength = 0;
    public int floatingElementConfigsLength = 0;
    public int scrollElementConfigsLength = 0;
    public int imageElementConfigsLength = 0;
    public int textElementConfigsLength = 0;
    public int customElementConfigsLength = 0;
    public int layoutElementHashMapItemPoolLength = 0;
    public int layoutElementTreeRootsLength = 0;
    public int measureTextHashMapInternalLength = 0;
    public int measureTextHashMapInternalFreeListLength = 0;
    public int measuredWordsLength = 0;
    public int measuredWordsFreeListLength = 0;
    public int textElementDataLength = 0;
    public int wrappedTextLinesLength = 0;
    public int imageElementPointersLength = 0;
    public int pointerOverIdsLength = 0;
    public int scrollContainerDatasLength = 0;
    public int resizableBufferLength = 0;

    public ClayJContext(int maxElementCount, int maxMeasureTextCacheWordCount) {
        this.maxElementCount = maxElementCount;
        this.maxMeasureTextCacheWordCount = maxMeasureTextCacheWordCount;

        scrollContainerDatas = new ScrollContainerDataInternal[MAX_SCROLL_CONTAINERS];
        for (int i = 0; i < scrollContainerDatas.length; i++)
            scrollContainerDatas[i] = new ScrollContainerDataInternal();

        layoutElements = new LayoutElement[maxElementCount];
        for (int i = 0; i < maxElementCount; i++) layoutElements[i] = new LayoutElement();

        renderCommands = new RenderCommand[maxElementCount];
        for (int i = 0; i < maxElementCount; i++) renderCommands[i] = new RenderCommand();

        openLayoutElementStack = new int[maxElementCount];
        layoutElementChildrenBuffer = new int[maxElementCount];
        layoutElementChildren = new int[maxElementCount];
        openClipElementStack = new int[maxElementCount];
        layoutElementClipElementIds = new int[maxElementCount];

        layoutConfigs = new LayoutConfigBuilder[maxElementCount];
        for (int i = 0; i < maxElementCount; i++) layoutConfigs[i] = new LayoutConfigBuilder();

        sharedElementConfigs = new SharedConfigBuilder[maxElementCount];
        for (int i = 0; i < maxElementCount; i++) sharedElementConfigs[i] = new SharedConfigBuilder();

        borderElementConfigs = new BorderConfigBuilder[maxElementCount];
        for (int i = 0; i < maxElementCount; i++) borderElementConfigs[i] = new BorderConfigBuilder();

        floatingElementConfigs = new FloatingConfigBuilder[maxElementCount];
        for (int i = 0; i < maxElementCount; i++) floatingElementConfigs[i] = new FloatingConfigBuilder();

        scrollElementConfigs = new ScrollConfigBuilder[maxElementCount];
        for (int i = 0; i < maxElementCount; i++) scrollElementConfigs[i] = new ScrollConfigBuilder();

        imageElementConfigs = new ImageConfigBuilder[maxElementCount];
        for (int i = 0; i < maxElementCount; i++) imageElementConfigs[i] = new ImageConfigBuilder();

        textElementConfigs = new TextConfigBuilder[maxElementCount];
        for (int i = 0; i < maxElementCount; i++) textElementConfigs[i] = new TextConfigBuilder();

        customElementConfigs = new CustomConfigBuilder[maxElementCount];
        for (int i = 0; i < maxElementCount; i++) customElementConfigs[i] = new CustomConfigBuilder();

        layoutElementHashMapItemPool = new LayoutElementHashMapItem[maxElementCount];
        for (int i = 0; i < maxElementCount; i++) layoutElementHashMapItemPool[i] = new LayoutElementHashMapItem();

        layoutElementsHashBuckets = new int[maxElementCount];
        Arrays.fill(layoutElementsHashBuckets, -1);

        layoutElementTreeRoots = new LayoutElementTreeRoot[maxElementCount];
        for (int i = 0; i < maxElementCount; i++) layoutElementTreeRoots[i] = new LayoutElementTreeRoot();

        textElementData = new TextElementData[maxElementCount];
        for (int i = 0; i < maxElementCount; i++) textElementData[i] = new TextElementData();

        wrappedTextLines = new WrappedTextLine[maxElementCount];
        for (int i = 0; i < maxElementCount; i++) wrappedTextLines[i] = new WrappedTextLine();

        imageElementPointers = new int[maxElementCount];
        pointerOverIds = new ElementId[maxElementCount];

        layoutElementTreeNodes = new LayoutElementTreeNode[maxElementCount];
        for (int i = 0; i < maxElementCount; i++) layoutElementTreeNodes[i] = new LayoutElementTreeNode();

        resizableBuffer = new int[maxElementCount];
        treeNodeVisited = new boolean[maxElementCount];

        measureTextHashMapInternal = new MeasureTextCacheItem[maxMeasureTextCacheWordCount];
        for (int i = 0; i < maxMeasureTextCacheWordCount; i++)
            measureTextHashMapInternal[i] = new MeasureTextCacheItem();
        measureTextHashMapInternalFreeList = new int[maxMeasureTextCacheWordCount];
        measureTextHashMap = new int[maxMeasureTextCacheWordCount];
        measuredWords = new MeasuredWord[maxMeasureTextCacheWordCount];
        for (int i = 0; i < maxMeasureTextCacheWordCount; i++) measuredWords[i] = new MeasuredWord();
        measuredWordsFreeList = new int[maxMeasureTextCacheWordCount];
        measureTextHashMapInternalLength = 1;

        int transientSize = maxElementCount / 2;

        transientDecls = new ArenaPool<>(transientSize, ElementDeclBuilder::new, ElementDeclBuilder::reset);
        transientLayouts = new ArenaPool<>(transientSize, LayoutConfigBuilder::new, LayoutConfigBuilder::reset);
        transientTexts = new ArenaPool<>(transientSize, TextConfigBuilder::new, TextConfigBuilder::reset);
        transientCustoms = new ArenaPool<>(transientSize, CustomConfigBuilder::new, CustomConfigBuilder::reset);
        transientIds = new ArenaPool<>(transientSize, ElementId::new, ElementId::reset);
    }

    public void resetEphemeral() {
        maxElementsExceeded = false;
        maxRenderCommandsExceeded = false;
        maxTextMeasureCacheExceeded = false;
        textMeasurementFunctionNotSet = false;

        layoutElementsLength = 0;
        renderCommandsLength = 0;
        openLayoutElementStackLength = 0;
        layoutElementChildrenBufferLength = 0;
        layoutElementChildrenLength = 0;
        openClipElementStackLength = 0;
        layoutElementTreeRootsLength = 0;
        layoutConfigsLength = 0;
        sharedElementConfigsLength = 0;
        borderElementConfigsLength = 0;
        floatingElementConfigsLength = 0;
        scrollElementConfigsLength = 0;
        imageElementConfigsLength = 0;
        textElementConfigsLength = 0;
        customElementConfigsLength = 0;
        textElementDataLength = 0;
        wrappedTextLinesLength = 0;
        imageElementPointersLength = 0;
        dynamicElementIndex = 0;

        transientDecls.reset();
        transientLayouts.reset();
        transientTexts.reset();
        transientCustoms.reset();
        transientIds.reset();
    }

    public LayoutElementHashMapItem getHashMapItem(int id) {
        if (id == 0) return null;
        int bucket = (id & 0x7FFFFFFF) % layoutElementsHashBuckets.length;
        int index = layoutElementsHashBuckets[bucket];

        while (index != -1) {
            LayoutElementHashMapItem item = layoutElementHashMapItemPool[index];
            if (item.elementId.id == id) return item;
            index = item.nextIndex;
        }
        return null;
    }

    public LayoutElementHashMapItem addHashMapItem(ElementId elementId, LayoutElement layoutElement, int elementIndex, int idAlias) {
        int bucket = (elementId.id & 0x7FFFFFFF) % layoutElementsHashBuckets.length;
        int prevIndex = -1;
        int currentIndex = layoutElementsHashBuckets[bucket];

        while (currentIndex != -1) {
            LayoutElementHashMapItem item = layoutElementHashMapItemPool[currentIndex];
            if (item.elementId.id == elementId.id) {
                if (item.generation < this.generation) {
                    item.elementId.set(elementId);
                    item.generation = this.generation;
                    item.layoutElement = layoutElement;
                    item.idAlias = idAlias;
                    item.onHoverFunction = null;
                    item.elementIndex = elementIndex;
                    return item;
                } else {
                    if (errorHandler != null) errorHandler.handleError(ClayJError.DUPLICATE_ID);
                    return item;
                }
            }
            prevIndex = currentIndex;
            currentIndex = item.nextIndex;
        }

        if (layoutElementHashMapItemPoolLength >= layoutElementHashMapItemPool.length) {
            if (errorHandler != null) errorHandler.handleError(ClayJError.ARENA_CAPACITY_EXCEEDED);
            return null;
        }

        int newItemIndex = layoutElementHashMapItemPoolLength++;
        LayoutElementHashMapItem newItem = layoutElementHashMapItemPool[newItemIndex];
        newItem.reset();

        newItem.elementId.set(elementId);
        newItem.layoutElement = layoutElement;
        newItem.generation = this.generation;
        newItem.idAlias = idAlias;
        newItem.elementIndex = elementIndex;

        if (prevIndex != -1) layoutElementHashMapItemPool[prevIndex].nextIndex = newItemIndex;
        else layoutElementsHashBuckets[bucket] = newItemIndex;

        return newItem;
    }

    public LayoutElement openLayoutElement() {
        return layoutElements[openLayoutElementStack[openLayoutElementStackLength - 1]];
    }

    public LayoutElement openParentLayoutElement() {
        return layoutElements[openLayoutElementStack[openLayoutElementStackLength - 2]];
    }
}