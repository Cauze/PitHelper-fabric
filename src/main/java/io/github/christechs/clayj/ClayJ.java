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
import io.github.christechs.clayj.enums.*;
import io.github.christechs.clayj.math.*;
import io.github.christechs.clayj.util.ArrayUtil;
import io.github.christechs.clayj.util.HashUtil;

import java.util.Arrays;

import static io.github.christechs.clayj.ClayJContext.MAX_SCROLL_CONTAINERS;

public final class ClayJ {

    private static final float EPS = 0.01f;
    private static final String ROOT_CONTAINER_ID = "Clay__RootContainer";

    private static final ThreadLocal<ClayJContext> threadContext = new ThreadLocal<>();

    private ClayJ() {
    }

    public static void initialize(int maxElements, int maxMeasureTextCache, Dimensions layoutDimensions) {
        ClayJContext ctx = new ClayJContext(maxElements, maxMeasureTextCache);
        ctx.layoutDimensions.set(layoutDimensions);
        threadContext.set(ctx);
    }

    public static ClayJContext getContext() {
        return threadContext.get();
    }

    public static void setLayoutDimensions(float width, float height) {
        getContext().layoutDimensions.set(width, height);
    }

    public static void setMeasureTextFunction(MeasureTextFunction fn) {
        getContext().measureTextFunction = fn;
    }

    public static void setErrorHandler(ErrorHandler handler) {
        getContext().errorHandler = handler;
    }

    public static void setQueryScrollOffsetFunction(QueryScrollOffsetFunction fn) {
        getContext().queryScrollOffsetFunction = fn;
    }

    public static void el(ElementDeclBuilder decl, Runnable children) {
        openElement();
        if (decl != null) configureOpenElement(decl);
        if (children != null) children.run();
        closeElement();
    }

    public static void el(Runnable children) {
        el(decl(), children);
    }

    public static void text(CharSequence text, TextConfigBuilder config) {
        openTextElement(text, config);
    }

    public static ElementDeclBuilder decl() {
        return getContext().transientDecls.take();
    }

    public static ElementDeclBuilder id(CharSequence idString) {
        return decl().id(idString);
    }

    public static LayoutConfigBuilder layout() {
        return getContext().transientLayouts.take();
    }

    public static TextConfigBuilder txt() {
        return getContext().transientTexts.take();
    }

    public static void setPointerState(Vector2 position, boolean isPointerDown) {
        ClayJContext context = getContext();
        if (context == null || context.maxElementsExceeded) return;
        context.pointerInfo.position.set(position);
        context.pointerOverIdsLength = 0;

        int dfsLen = 0;
        for (int rootIndex = context.layoutElementTreeRootsLength - 1; rootIndex >= 0; --rootIndex) {
            dfsLen = 0;
            LayoutElementTreeRoot root = context.layoutElementTreeRoots[rootIndex];
            context.layoutElementChildrenBuffer[dfsLen++] = root.layoutElementIndex;
            Arrays.fill(context.treeNodeVisited, false);
            boolean found = false;

            while (dfsLen > 0) {
                if (context.treeNodeVisited[dfsLen - 1]) {
                    dfsLen--;
                    continue;
                }
                context.treeNodeVisited[dfsLen - 1] = true;

                int currentElementIndex = context.layoutElementChildrenBuffer[dfsLen - 1];
                LayoutElement currentEl = context.layoutElements[currentElementIndex];

                LayoutElementHashMapItem mapItem = context.getHashMapItem(currentEl.id);
                int clipElementId = context.layoutElementClipElementIds[currentElementIndex];

                if (mapItem != null) {
                    BoundingBox box = mapItem.boundingBox;
                    float checkX = position.x;
                    float checkY = position.y;

                    boolean inside = (checkX >= box.x && checkX <= box.x + box.width && checkY >= box.y && checkY <= box.y + box.height);
                    boolean clipPass;
                    if (context.externalScrollHandlingEnabled) {
                        clipPass = true;
                    } else {
                        clipPass = true;
                        int currentClipId = clipElementId;
                        while (currentClipId != 0) {
                            LayoutElementHashMapItem clipItem = context.getHashMapItem(currentClipId);
                            if (clipItem == null) {
                                clipPass = false;
                                break;
                            }
                            BoundingBox clipBox = clipItem.boundingBox;
                            if (!(checkX >= clipBox.x && checkX <= clipBox.x + clipBox.width &&
                                    checkY >= clipBox.y && checkY <= clipBox.y + clipBox.height)) {
                                clipPass = false;
                                break;
                            }
                            int clipElementIdx = clipItem.elementIndex;
                            if (clipElementIdx < 0) break;
                            currentClipId = context.layoutElementClipElementIds[clipElementIdx];
                        }
                    }
                    if (inside && clipPass) {
                        if (mapItem.onHoverFunction != null) mapItem.onHoverFunction.run();
                        context.pointerOverIds[context.pointerOverIdsLength++] = mapItem.elementId;
                        found = true;
                    }

                    if (currentEl.isTextElement) {
                        dfsLen--;
                        continue;
                    }

                    for (int i = currentEl.childrenLength - 1; i >= 0; --i) {
                        context.layoutElementChildrenBuffer[dfsLen] = context.layoutElementChildren[currentEl.childrenStart + i];
                        context.treeNodeVisited[dfsLen++] = false;
                    }
                } else {
                    dfsLen--;
                }
            }

            LayoutElement rootElement = context.layoutElements[root.layoutElementIndex];
            FloatingConfigBuilder fl = (FloatingConfigBuilder) rootElement.getConfig(ElementConfigType.FLOATING);
            if (found && fl != null && fl.captureMode == PointerCaptureMode.CAPTURE) break;
        }

        if (isPointerDown) {
            if (context.pointerInfo.state == InteractionState.PRESSED_THIS_FRAME)
                context.pointerInfo.state = InteractionState.PRESSED;
            else if (context.pointerInfo.state != InteractionState.PRESSED)
                context.pointerInfo.state = InteractionState.PRESSED_THIS_FRAME;
        } else {
            if (context.pointerInfo.state == InteractionState.RELEASED_THIS_FRAME)
                context.pointerInfo.state = InteractionState.RELEASED;
            else if (context.pointerInfo.state != InteractionState.RELEASED)
                context.pointerInfo.state = InteractionState.RELEASED_THIS_FRAME;
        }
    }

    public static boolean pointerOver(CharSequence idString) {
        ClayJContext context = getContext();
        if (context == null || context.maxElementsExceeded) return false;
        int targetId = HashUtil.hashString(idString, 0, 0);
        for (int i = 0; i < context.pointerOverIdsLength; i++) {
            if (context.pointerOverIds[i].id == targetId) return true;
        }
        return false;
    }

    public static boolean hovered() {
        ClayJContext context = getContext();
        if (context == null || context.maxElementsExceeded) return false;
        LayoutElement el = context.openLayoutElement();
        if (el.id == 0) generateAnonId(el, true);
        for (int i = 0; i < context.pointerOverIdsLength; i++) {
            if (context.pointerOverIds[i].id == el.id) return true;
        }
        return false;
    }

    public static void onHover(Runnable onHoverFunction) {
        ClayJContext context = getContext();
        if (context == null || context.maxElementsExceeded) return;
        LayoutElement el = context.openLayoutElement();
        if (el.id == 0) generateAnonId(el, true);
        LayoutElementHashMapItem item = context.getHashMapItem(el.id);
        if (item != null) item.onHoverFunction = onHoverFunction;
    }

    public static void updateScrollContainers(boolean enableDragScrolling, Vector2 scrollDelta, float deltaTime) {
        ClayJContext context = getContext();
        if (context == null || context.maxElementsExceeded) return;
        boolean isPointerActive = enableDragScrolling && (context.pointerInfo.state == InteractionState.PRESSED || context.pointerInfo.state == InteractionState.PRESSED_THIS_FRAME);

        int highestPriorityElementIndex = -1;
        ScrollContainerDataInternal highestPriorityScrollData = null;

        for (int i = 0; i < context.scrollContainerDatasLength; i++) {
            ScrollContainerDataInternal scrollData = context.scrollContainerDatas[i];
            if (!scrollData.openThisFrame) {
                ArrayUtil.removeSwapback(context.scrollContainerDatas, context.scrollContainerDatasLength, i);
                context.scrollContainerDatasLength--;
                i--;
                continue;
            }
            scrollData.openThisFrame = false;
            if (context.getHashMapItem(scrollData.elementId) == null) {
                ArrayUtil.removeSwapback(context.scrollContainerDatas, context.scrollContainerDatasLength, i);
                context.scrollContainerDatasLength--;
                i--;
                continue;
            }

            if (!isPointerActive && scrollData.pointerScrollActive) {
                float xDiff = scrollData.scrollPosition.x - scrollData.scrollOrigin.x;
                if (xDiff < -10 || xDiff > 10)
                    scrollData.scrollMomentum.x = (scrollData.scrollPosition.x - scrollData.scrollOrigin.x) / (scrollData.momentumTime * 25);
                float yDiff = scrollData.scrollPosition.y - scrollData.scrollOrigin.y;
                if (yDiff < -10 || yDiff > 10)
                    scrollData.scrollMomentum.y = (scrollData.scrollPosition.y - scrollData.scrollOrigin.y) / (scrollData.momentumTime * 25);
                scrollData.pointerScrollActive = false;
                scrollData.pointerOrigin.set(0, 0);
                scrollData.scrollOrigin.set(0, 0);
                scrollData.momentumTime = 0;
            }

            scrollData.scrollPosition.x += scrollData.scrollMomentum.x;
            scrollData.scrollMomentum.x *= 0.95f;
            boolean scrollOccurred = scrollDelta.x != 0 || scrollDelta.y != 0;
            if ((scrollData.scrollMomentum.x > -0.1f && scrollData.scrollMomentum.x < 0.1f) || scrollOccurred)
                scrollData.scrollMomentum.x = 0;
            scrollData.scrollPosition.x = Math.max(Math.min(scrollData.scrollPosition.x, 0), -(Math.max(scrollData.contentSize.width - scrollData.layoutElement.dimensions.width, 0)));

            scrollData.scrollPosition.y += scrollData.scrollMomentum.y;
            scrollData.scrollMomentum.y *= 0.95f;
            if ((scrollData.scrollMomentum.y > -0.1f && scrollData.scrollMomentum.y < 0.1f) || scrollOccurred)
                scrollData.scrollMomentum.y = 0;
            scrollData.scrollPosition.y = Math.max(Math.min(scrollData.scrollPosition.y, 0), -(Math.max(scrollData.contentSize.height - scrollData.layoutElement.dimensions.height, 0)));

            for (int j = 0; j < context.pointerOverIdsLength; ++j) {
                if (scrollData.layoutElement.id == context.pointerOverIds[j].id) {
                    highestPriorityElementIndex = j;
                    highestPriorityScrollData = scrollData;
                }
            }
        }

        if (highestPriorityElementIndex > -1 && highestPriorityScrollData != null) {
            LayoutElement scrollElement = highestPriorityScrollData.layoutElement;
            ScrollConfigBuilder clipConfig = (ScrollConfigBuilder) scrollElement.getConfig(ElementConfigType.SCROLL);
            boolean canScrollVertically = clipConfig.vertical && highestPriorityScrollData.contentSize.height > scrollElement.dimensions.height;
            boolean canScrollHorizontally = clipConfig.horizontal && highestPriorityScrollData.contentSize.width > scrollElement.dimensions.width;

            if (canScrollVertically) highestPriorityScrollData.scrollPosition.y += scrollDelta.y * 10;
            if (canScrollHorizontally) highestPriorityScrollData.scrollPosition.x += scrollDelta.x * 10;

            if (isPointerActive) {
                highestPriorityScrollData.scrollMomentum.set(0, 0);
                if (!highestPriorityScrollData.pointerScrollActive) {
                    highestPriorityScrollData.pointerOrigin.set(context.pointerInfo.position);
                    highestPriorityScrollData.scrollOrigin.set(highestPriorityScrollData.scrollPosition);
                    highestPriorityScrollData.pointerScrollActive = true;
                } else {
                    float scrollDeltaX = 0, scrollDeltaY = 0;
                    if (canScrollHorizontally) {
                        float oldX = highestPriorityScrollData.scrollPosition.x;
                        highestPriorityScrollData.scrollPosition.x = highestPriorityScrollData.scrollOrigin.x + (context.pointerInfo.position.x - highestPriorityScrollData.pointerOrigin.x);
                        highestPriorityScrollData.scrollPosition.x = Math.max(Math.min(highestPriorityScrollData.scrollPosition.x, 0), -(highestPriorityScrollData.contentSize.width - highestPriorityScrollData.layoutElement.dimensions.width));
                        scrollDeltaX = highestPriorityScrollData.scrollPosition.x - oldX;
                    }
                    if (canScrollVertically) {
                        float oldY = highestPriorityScrollData.scrollPosition.y;
                        highestPriorityScrollData.scrollPosition.y = highestPriorityScrollData.scrollOrigin.y + (context.pointerInfo.position.y - highestPriorityScrollData.pointerOrigin.y);
                        highestPriorityScrollData.scrollPosition.y = Math.max(Math.min(highestPriorityScrollData.scrollPosition.y, 0), -(highestPriorityScrollData.contentSize.height - highestPriorityScrollData.layoutElement.dimensions.height));
                        scrollDeltaY = highestPriorityScrollData.scrollPosition.y - oldY;
                    }
                    if (scrollDeltaX > -0.1f && scrollDeltaX < 0.1f && scrollDeltaY > -0.1f && scrollDeltaY < 0.1f && highestPriorityScrollData.momentumTime > 0.15f) {
                        highestPriorityScrollData.momentumTime = 0;
                        highestPriorityScrollData.pointerOrigin.set(context.pointerInfo.position);
                        highestPriorityScrollData.scrollOrigin.set(highestPriorityScrollData.scrollPosition);
                    } else {
                        highestPriorityScrollData.momentumTime += deltaTime;
                    }
                }
            }
            if (canScrollVertically)
                highestPriorityScrollData.scrollPosition.y = Math.max(Math.min(highestPriorityScrollData.scrollPosition.y, 0), -(highestPriorityScrollData.contentSize.height - scrollElement.dimensions.height));
            if (canScrollHorizontally)
                highestPriorityScrollData.scrollPosition.x = Math.max(Math.min(highestPriorityScrollData.scrollPosition.x, 0), -(highestPriorityScrollData.contentSize.width - scrollElement.dimensions.width));
        }
    }

    public static boolean getScrollContainerData(ElementId id, ScrollContainerData outData) {
        ClayJContext context = getContext();
        for (int i = 0; i < context.scrollContainerDatasLength; i++) {
            if (context.scrollContainerDatas[i].elementId == id.id) {
                outData.scrollPosition = context.scrollContainerDatas[i].scrollPosition;
                LayoutElementHashMapItem mapItem = context.getHashMapItem(id.id);
                if (mapItem != null) {
                    outData.scrollContainerDimensions.set(mapItem.boundingBox.width, mapItem.boundingBox.height);
                } else {
                    outData.scrollContainerDimensions.set(0, 0);
                }
                outData.contentDimensions.set(context.scrollContainerDatas[i].contentSize);
                outData.config = (ScrollConfigBuilder) context.scrollContainerDatas[i].layoutElement.getConfig(ElementConfigType.SCROLL);
                outData.found = true;
                return true;
            }
        }
        outData.found = false;
        return false;
    }

    private static MeasureTextCacheItem measureTextCached(CharSequence text, TextConfigBuilder config) {
        ClayJContext context = getContext();
        if (context.measureTextFunction == null)
            return context.measureTextHashMapInternal[0];

        int id = HashUtil.hashString(text, config.fontId, config.fontSize);
        int hashBucket = Math.abs(id % (context.maxMeasureTextCacheWordCount / 32));
        int elementIndexPrev = 0;
        int elementIndex = context.measureTextHashMap[hashBucket];

        while (elementIndex != 0) {
            MeasureTextCacheItem hashEntry = context.measureTextHashMapInternal[elementIndex];
            if (hashEntry.id == id) {
                hashEntry.generation = context.generation;
                return hashEntry;
            }
            if (context.generation - hashEntry.generation > 2) {
                int nextWordIdx = hashEntry.measureWordsStartIndex;
                while (nextWordIdx != -1) {
                    MeasuredWord mw = context.measuredWords[nextWordIdx];
                    context.measuredWordsFreeList[context.measuredWordsFreeListLength++] = nextWordIdx;
                    nextWordIdx = mw.next;
                }
                int nextIdx = hashEntry.nextIndex;
                hashEntry.measureWordsStartIndex = -1;
                context.measureTextHashMapInternalFreeList[context.measureTextHashMapInternalFreeListLength++] = elementIndex;
                if (elementIndexPrev == 0) context.measureTextHashMap[hashBucket] = nextIdx;
                else context.measureTextHashMapInternal[elementIndexPrev].nextIndex = nextIdx;
                elementIndex = nextIdx;
            } else {
                elementIndexPrev = elementIndex;
                elementIndex = hashEntry.nextIndex;
            }
        }

        int newItemIdx = 0;
        if (context.measureTextHashMapInternalFreeListLength > 0) {
            newItemIdx = context.measureTextHashMapInternalFreeList[--context.measureTextHashMapInternalFreeListLength];
        } else {
            if (context.measureTextHashMapInternalLength >= context.maxMeasureTextCacheWordCount) {
                if (!context.maxTextMeasureCacheExceeded && context.errorHandler != null) {
                    context.maxTextMeasureCacheExceeded = true;
                    context.errorHandler.handleError(ClayJError.TEXT_MEASUREMENT_CAPACITY_EXCEEDED);
                }
                return context.measureTextHashMapInternal[0];
            }
            newItemIdx = context.measureTextHashMapInternalLength++;
        }

        MeasureTextCacheItem measured = context.measureTextHashMapInternal[newItemIdx];
        measured.reset();
        measured.id = id;
        measured.generation = context.generation;

        int start = 0, end = 0;
        float lineWidth = 0f, measuredWidth = 0f, measuredHeight = 0f;

        context.measureTextFunction.measure(" ", 0, 1, config, context.scratchDimensions);
        float spaceWidth = context.scratchDimensions.width;

        MeasuredWord tempWord = new MeasuredWord();
        MeasuredWord previousWord = tempWord;

        while (end < text.length()) {
            char current = text.charAt(end);
            if (current == ' ' || current == '\n') {
                int length = end - start;
                Dimensions dim = context.scratchDimensions;
                dim.set(0, 0);

                if (length > 0) {
                    context.measureTextFunction.measure(text, start, length, config, dim);
                }

                measuredHeight = Math.max(measuredHeight, dim.height);
                if (current == ' ') {
                    if (context.measuredWordsLength >= context.maxMeasureTextCacheWordCount) {
                        if (!context.maxTextMeasureCacheExceeded && context.errorHandler != null) {
                            context.maxTextMeasureCacheExceeded = true;
                            context.errorHandler.handleError(ClayJError.TEXT_MEASUREMENT_CAPACITY_EXCEEDED);
                        }
                        break;
                    }
                    dim.width += spaceWidth;
                    previousWord = addMeasuredWord(start, length + 1, dim.width, previousWord);
                    lineWidth += dim.width;
                }
                if (current == '\n') {
                    if (context.measuredWordsLength >= context.maxMeasureTextCacheWordCount) {
                        if (!context.maxTextMeasureCacheExceeded && context.errorHandler != null) {
                            context.maxTextMeasureCacheExceeded = true;
                            context.errorHandler.handleError(ClayJError.TEXT_MEASUREMENT_CAPACITY_EXCEEDED);
                        }
                        break;
                    }
                    if (length > 0) previousWord = addMeasuredWord(start, length, dim.width, previousWord);
                    previousWord = addMeasuredWord(end + 1, 0, 0f, previousWord);
                    lineWidth += dim.width;
                    measuredWidth = Math.max(lineWidth, measuredWidth);
                    measured.containsNewlines = true;
                    lineWidth = 0;
                }
                start = end + 1;
            }
            end++;
        }
        if (end - start > 0) {
            Dimensions dim = context.scratchDimensions;
            dim.set(0, 0);
            context.measureTextFunction.measure(text, start, end - start, config, dim);
            addMeasuredWord(start, end - start, dim.width, previousWord);
            lineWidth += dim.width;
            measuredHeight = Math.max(measuredHeight, dim.height);
        }
        measuredWidth = Math.max(lineWidth, measuredWidth) - config.letterSpacing;

        measured.measureWordsStartIndex = tempWord.next;
        measured.unwrappedDimensions.set(measuredWidth, measuredHeight);

        if (elementIndexPrev != 0) context.measureTextHashMapInternal[elementIndexPrev].nextIndex = newItemIdx;
        else context.measureTextHashMap[hashBucket] = newItemIdx;

        return measured;
    }

    private static MeasuredWord addMeasuredWord(int start, int length, float width, MeasuredWord previousWord) {
        ClayJContext context = getContext();
        int idx = 0;
        if (context.measuredWordsFreeListLength > 0) {
            idx = context.measuredWordsFreeList[--context.measuredWordsFreeListLength];
        } else {
            idx = context.measuredWordsLength++;
        }
        MeasuredWord w = context.measuredWords[idx];
        w.startOffset = start;
        w.length = length;
        w.width = width;
        w.next = -1;
        previousWord.next = idx;
        return w;
    }

    public static void beginLayout() {
        ClayJContext context = getContext();
        if (context == null) {
            System.err.println("ClayJ Error: ClayJ not initialized. Call ClayJ.initialize() before beginning layout.");
            return;
        }

        context.resetEphemeral();
        context.generation++;

        openElement();
        LayoutConfigBuilder rootLayout = context.layoutConfigs[context.layoutConfigsLength++];
        rootLayout.reset();
        rootLayout.sizing.width.type = SizingType.FIXED;
        rootLayout.sizing.width.minMax.min = context.layoutDimensions.width;
        rootLayout.sizing.width.minMax.max = context.layoutDimensions.width;
        rootLayout.sizing.height.type = SizingType.FIXED;
        rootLayout.sizing.height.minMax.min = context.layoutDimensions.height;
        rootLayout.sizing.height.minMax.max = context.layoutDimensions.height;

        ElementDeclBuilder rootDecl = context.rootDeclBuilder;
        rootDecl.reset();
        rootDecl.id(ROOT_CONTAINER_ID);
        rootDecl.layout = rootLayout;
        configureOpenElement(rootDecl);

        LayoutElementTreeRoot treeRoot = context.layoutElementTreeRoots[context.layoutElementTreeRootsLength++];
        treeRoot.reset();
        treeRoot.layoutElementIndex = 0;
    }

    public static LayoutResults endLayout() {
        ClayJContext context = getContext();
        if (context == null) {
            return new LayoutResults(new RenderCommand[0], 0);
        }

        closeElement();
        if (context.openLayoutElementStackLength > 1 && context.errorHandler != null) {
            context.errorHandler.handleError(ClayJError.UNBALANCED_OPEN_CLOSE);
        }
        if (!context.maxElementsExceeded) {
            calculateFinalLayout();
        }
        return new LayoutResults(context.renderCommands, context.renderCommandsLength);
    }

    public static void openElement() {
        ClayJContext context = getContext();
        if (context == null || context.maxElementsExceeded) return;
        if (context.layoutElementsLength >= context.maxElementCount - 1) {
            context.maxElementsExceeded = true;
            if (context.errorHandler != null)
                context.errorHandler.handleError(ClayJError.ELEMENTS_CAPACITY_EXCEEDED);
            return;
        }

        int elemIdx = context.layoutElementsLength++;
        context.layoutElements[elemIdx].reset();

        if (context.openLayoutElementStackLength > 0) {
            context.openLayoutElement().childrenLength++;
        }

        context.openLayoutElementStack[context.openLayoutElementStackLength++] = elemIdx;
        context.layoutElementClipElementIds[elemIdx] = (context.openClipElementStackLength > 0)
                ? context.openClipElementStack[context.openClipElementStackLength - 1] : 0;
    }

    public static void configureOpenElement(ElementDeclBuilder decl) {
        ClayJContext context = getContext();
        if (context == null || context.maxElementsExceeded) return;
        LayoutElement el = context.openLayoutElement();

        if (decl.layout != null) {
            el.layoutConfig = decl.layout;
            if ((decl.layout.sizing.width.type == SizingType.PERCENT && decl.layout.sizing.width.percent > 1f) ||
                    (decl.layout.sizing.height.type == SizingType.PERCENT && decl.layout.sizing.height.percent > 1f)) {
                if (context.errorHandler != null)
                    context.errorHandler.handleError(ClayJError.PERCENTAGE_OVER_1);
            }
        } else {
            LayoutConfigBuilder defaultCfg = context.layoutConfigs[context.layoutConfigsLength++];
            defaultCfg.reset();
            el.layoutConfig = defaultCfg;
        }

        if (decl.backgroundColor != null || decl.cornerRadius != null || decl.userData != null) {
            SharedConfigBuilder dst = context.sharedElementConfigs[context.sharedElementConfigsLength++];
            dst.reset();

            if (decl.backgroundColor != null) {
                if (dst.backgroundColor == null) dst.backgroundColor = new Color();
                dst.backgroundColor.set(decl.backgroundColor);
            }
            if (decl.cornerRadius != null) {
                if (dst.cornerRadius == null) dst.cornerRadius = new CornerRadius();
                dst.cornerRadius.set(decl.cornerRadius);
            }
            if (decl.userData != null) dst.userData = decl.userData;
            el.attachConfig(ElementConfigType.SHARED, dst);
        }

        if (decl.image != null && decl.image.imageData != null) {
            ImageConfigBuilder dst = context.imageElementConfigs[context.imageElementConfigsLength++];
            dst.reset();
            dst.set(decl.image);
            el.attachConfig(ElementConfigType.IMAGE, dst);
            context.imageElementPointers[context.imageElementPointersLength++] = context.openLayoutElementStack[context.openLayoutElementStackLength - 1];
        }

        if (decl.custom != null && decl.custom.customData != null) {
            CustomConfigBuilder dst = context.customElementConfigs[context.customElementConfigsLength++];
            dst.reset();
            dst.set(decl.custom);
            el.attachConfig(ElementConfigType.CUSTOM, dst);
        }

        ElementId resolvedId = decl.id;

        if (decl.floating != null && decl.floating.attachTo != AttachToElement.NONE && context.openLayoutElementStackLength >= 2) {
            LayoutElement parent = context.openParentLayoutElement();
            FloatingConfigBuilder dst = context.floatingElementConfigs[context.floatingElementConfigsLength++];
            dst.reset();
            dst.set(decl.floating);

            int clipId = 0;
            switch (dst.attachTo) {
                case PARENT:
                    dst.parentId = parent.id;
                    if (context.openClipElementStackLength > 0)
                        clipId = context.openClipElementStack[context.openClipElementStackLength - 1];
                    break;
                case ELEMENT_WITH_ID:
                    dst.parentId = decl.floating.parentId;
                    break;
                case ROOT:
                    dst.parentId = HashUtil.hashString(ROOT_CONTAINER_ID, 0, 0);
                    break;
            }

            if (resolvedId == null || resolvedId.id == 0) {
                resolvedId = context.transientIds.take();
                HashUtil.hashString("Clay__FloatingContainer", context.layoutElementTreeRootsLength, 0, resolvedId);
            }

            LayoutElementTreeRoot treeRoot = context.layoutElementTreeRoots[context.layoutElementTreeRootsLength++];
            treeRoot.reset();
            treeRoot.layoutElementIndex = context.openLayoutElementStack[context.openLayoutElementStackLength - 1];
            treeRoot.parentId = dst.parentId;
            treeRoot.zIndex = dst.zIndex;
            treeRoot.clipElementId = clipId;
            el.attachConfig(ElementConfigType.FLOATING, dst);
        }

        if (resolvedId != null && resolvedId.id != 0) {
            el.id = resolvedId.id;
            context.addHashMapItem(resolvedId, el, context.openLayoutElementStack[context.openLayoutElementStackLength - 1], 0);
        } else if (el.id == 0) {
            generateAnonId(el, true);
        }

        if (decl.scroll != null && (decl.scroll.horizontal || decl.scroll.vertical)) {
            ScrollConfigBuilder dst = context.scrollElementConfigs[context.scrollElementConfigsLength++];
            dst.reset();
            dst.set(decl.scroll);
            el.attachConfig(ElementConfigType.SCROLL, dst);

            ScrollContainerDataInternal sd = null;
            for (int i = 0; i < context.scrollContainerDatasLength; i++) {
                if (context.scrollContainerDatas[i].elementId == el.id) {
                    sd = context.scrollContainerDatas[i];
                    sd.layoutElement = el;
                    sd.openThisFrame = true;
                    break;
                }
            }
            if (sd == null) {
                if (context.scrollContainerDatasLength >= MAX_SCROLL_CONTAINERS) {
                    if (context.errorHandler != null) {
                        context.errorHandler.handleError(ClayJError.ELEMENTS_CAPACITY_EXCEEDED);
                    }
                } else {
                    sd = context.scrollContainerDatas[context.scrollContainerDatasLength++];

                    if (sd == null) {
                        sd = new ScrollContainerDataInternal();
                        context.scrollContainerDatas[context.scrollContainerDatasLength - 1] = sd;
                    }

                    sd.reset();
                    sd.layoutElement = el;
                    sd.elementId = el.id;
                    sd.openThisFrame = true;
                }
            }

            if (context.externalScrollHandlingEnabled && context.queryScrollOffsetFunction != null) {
                Vector2 externalScroll = context.queryScrollOffsetFunction.query(sd.elementId);
                if (externalScroll != null) {
                    sd.scrollPosition.set(externalScroll);
                }
            }
            context.openClipElementStack[context.openClipElementStackLength++] = el.id;
        }

        if (decl.border != null) {
            BorderConfigBuilder dst = context.borderElementConfigs[context.borderElementConfigsLength++];
            dst.reset();
            dst.set(decl.border);
            el.attachConfig(ElementConfigType.BORDER, dst);
        }
    }

    public static void openTextElement(CharSequence text, TextConfigBuilder config) {
        ClayJContext context = getContext();
        if (context == null || context.maxElementsExceeded) return;
        if (context.layoutElementsLength >= context.maxElementCount - 1) {
            context.maxElementsExceeded = true;
            if (context.errorHandler != null)
                context.errorHandler.handleError(ClayJError.ELEMENTS_CAPACITY_EXCEEDED);
            return;
        }

        int elemIdx = context.layoutElementsLength++;
        LayoutElement el = context.layoutElements[elemIdx];
        el.reset();
        el.isTextElement = true;

        context.openLayoutElement().childrenLength++;

        context.layoutElementClipElementIds[elemIdx] = (context.openClipElementStackLength > 0)
                ? context.openClipElementStack[context.openClipElementStackLength - 1] : 0;

        LayoutConfigBuilder lc = context.layoutConfigs[context.layoutConfigsLength++];
        lc.reset();
        el.layoutConfig = lc;

        TextConfigBuilder dst = context.textElementConfigs[context.textElementConfigsLength++];
        dst.reset();
        dst.set(config);
        el.attachConfig(ElementConfigType.TEXT, dst);

        generateAnonId(el, false);

        TextElementData ted = context.textElementData[context.textElementDataLength++];
        ted.reset();
        ted.text = text;
        ted.elementIndex = elemIdx;
        el.textElementDataIndex = context.textElementDataLength - 1;

        MeasureTextCacheItem measured = measureTextCached(text, dst);
        ted.preferredDimensions.set(measured.unwrappedDimensions);
        el.dimensions.set(measured.unwrappedDimensions);

        if (dst.wrapMode == TextWrapMode.WORDS) {
            el.minDimensions.width = 0f;
        } else {
            el.minDimensions.width = measured.unwrappedDimensions.width;
        }
        el.minDimensions.height = measured.unwrappedDimensions.height;

        context.layoutElementChildrenBuffer[context.layoutElementChildrenBufferLength++] = elemIdx;
    }

    public static void closeElement() {
        ClayJContext context = getContext();
        if (context == null || context.maxElementsExceeded || context.openLayoutElementStackLength == 0) return;

        LayoutElement el = context.openLayoutElement();
        LayoutConfigBuilder cfg = el.layoutConfig;

        boolean hasScrollH = false, hasScrollV = false;
        ConfigBuilder scrollCfgObj = el.getConfig(ElementConfigType.SCROLL);
        if (scrollCfgObj instanceof ScrollConfigBuilder) {
            hasScrollH = ((ScrollConfigBuilder) scrollCfgObj).horizontal;
            hasScrollV = ((ScrollConfigBuilder) scrollCfgObj).vertical;
            if (context.openClipElementStackLength > 0) context.openClipElementStackLength--;
        }

        int childCount = el.childrenLength;
        el.childrenStart = context.layoutElementChildrenLength;
        int scratchTail = context.layoutElementChildrenBufferLength - childCount;
        for (int i = 0; i < childCount; i++) {
            context.layoutElementChildren[context.layoutElementChildrenLength++] = context.layoutElementChildrenBuffer[scratchTail + i];
        }
        context.layoutElementChildrenBufferLength -= childCount;

        float childGap = Math.max(childCount - 1, 0) * cfg.childGap;

        if (cfg.direction == LayoutDirection.LEFT_TO_RIGHT) {
            el.dimensions.width = cfg.padding.left + cfg.padding.right;
            for (int i = 0; i < childCount; i++) {
                LayoutElement child = context.layoutElements[context.layoutElementChildren[el.childrenStart + i]];
                el.dimensions.width += child.dimensions.width;
                el.dimensions.height = Math.max(el.dimensions.height, child.dimensions.height + cfg.padding.top + cfg.padding.bottom);
                if (!hasScrollH) el.minDimensions.width += child.minDimensions.width;
                if (!hasScrollV)
                    el.minDimensions.height = Math.max(el.minDimensions.height, child.minDimensions.height + cfg.padding.top + cfg.padding.bottom);
            }
            el.dimensions.width += childGap;
            el.minDimensions.width += childGap;
        } else {
            el.dimensions.height = cfg.padding.top + cfg.padding.bottom;
            for (int i = 0; i < childCount; i++) {
                LayoutElement child = context.layoutElements[context.layoutElementChildren[el.childrenStart + i]];
                el.dimensions.height += child.dimensions.height;
                el.dimensions.width = Math.max(el.dimensions.width, child.dimensions.width + cfg.padding.left + cfg.padding.right);
                if (!hasScrollV) el.minDimensions.height += child.minDimensions.height;
                if (!hasScrollH)
                    el.minDimensions.width = Math.max(el.minDimensions.width, child.minDimensions.width + cfg.padding.left + cfg.padding.right);
            }
            el.dimensions.height += childGap;
            el.minDimensions.height += childGap;
        }

        SizingAxis wAxis = cfg.sizing.width;
        if (wAxis.type != SizingType.PERCENT) {
            float maxW = wAxis.minMax.max > 0f ? wAxis.minMax.max : Float.MAX_VALUE;
            el.dimensions.width = Math.max(wAxis.minMax.min, Math.min(maxW, el.dimensions.width));
            el.minDimensions.width = Math.max(wAxis.minMax.min, Math.min(maxW, el.minDimensions.width));
        } else {
            el.dimensions.width = 0f;
            el.minDimensions.width = 0f;
        }

        SizingAxis hAxis = cfg.sizing.height;
        if (hAxis.type != SizingType.PERCENT) {
            float maxH = hAxis.minMax.max > 0f ? hAxis.minMax.max : Float.MAX_VALUE;
            el.dimensions.height = Math.max(hAxis.minMax.min, Math.min(maxH, el.dimensions.height));
            el.minDimensions.height = Math.max(hAxis.minMax.min, Math.min(maxH, el.minDimensions.height));
        } else {
            el.dimensions.height = 0f;
            el.minDimensions.height = 0f;
        }

        boolean isFloating = el.getConfig(ElementConfigType.FLOATING) != null;
        int closingIdx = context.openLayoutElementStack[--context.openLayoutElementStackLength];

        if (isFloating) {
            if (context.openLayoutElementStackLength > 0) {
                LayoutElement parent = context.openLayoutElement();
                parent.childrenLength--;
            }
        } else {
            if (context.openLayoutElementStackLength > 0) {
                context.layoutElementChildrenBuffer[context.layoutElementChildrenBufferLength++] = closingIdx;
            }
        }
    }

    private static void calculateFinalLayout() {
        ClayJContext context = getContext();
        sizeContainersAlongAxis(true);

        wrapTextElements();
        fixImageAspectRatios();

        int dfsLen = 0;
        for (int i = 0; i < context.layoutElementTreeRootsLength; i++) {
            context.treeNodeVisited[dfsLen] = false;
            LayoutElementTreeNode node = context.layoutElementTreeNodes[dfsLen++];
            node.layoutElement = context.layoutElements[context.layoutElementTreeRoots[i].layoutElementIndex];
        }

        while (dfsLen > 0) {
            LayoutElementTreeNode currentElementTreeNode = context.layoutElementTreeNodes[dfsLen - 1];
            LayoutElement currentEl = currentElementTreeNode.layoutElement;

            if (!context.treeNodeVisited[dfsLen - 1]) {
                context.treeNodeVisited[dfsLen - 1] = true;
                if (currentEl.isTextElement || currentEl.childrenLength == 0) {
                    dfsLen--;
                    continue;
                }
                for (int i = 0; i < currentEl.childrenLength; i++) {
                    context.treeNodeVisited[dfsLen] = false;
                    LayoutElementTreeNode nextNode = context.layoutElementTreeNodes[dfsLen++];
                    nextNode.layoutElement = context.layoutElements[context.layoutElementChildren[currentEl.childrenStart + i]];
                }
                continue;
            }
            dfsLen--;

            LayoutConfigBuilder cfg = currentEl.layoutConfig;
            float maxH = cfg.sizing.height.minMax.max > 0 ? cfg.sizing.height.minMax.max : Float.MAX_VALUE;

            if (cfg.direction == LayoutDirection.LEFT_TO_RIGHT) {
                for (int i = 0; i < currentEl.childrenLength; i++) {
                    LayoutElement child = context.layoutElements[context.layoutElementChildren[currentEl.childrenStart + i]];
                    float childH = Math.max(child.dimensions.height + cfg.padding.top + cfg.padding.bottom, currentEl.dimensions.height);
                    currentEl.dimensions.height = Math.max(cfg.sizing.height.minMax.min, Math.min(maxH, childH));
                }
            } else {
                float contentH = cfg.padding.top + cfg.padding.bottom;
                for (int i = 0; i < currentEl.childrenLength; i++) {
                    LayoutElement child = context.layoutElements[context.layoutElementChildren[currentEl.childrenStart + i]];
                    contentH += child.dimensions.height;
                }
                contentH += Math.max(currentEl.childrenLength - 1, 0) * cfg.childGap;
                currentEl.dimensions.height = Math.max(cfg.sizing.height.minMax.min, Math.min(maxH, contentH));
            }
        }

        sizeContainersAlongAxis(false);

        int sortMax = context.layoutElementTreeRootsLength - 1;
        while (sortMax > 0) {
            for (int i = 0; i < sortMax; i++) {
                LayoutElementTreeRoot curr = context.layoutElementTreeRoots[i];
                LayoutElementTreeRoot next = context.layoutElementTreeRoots[i + 1];
                if (next.zIndex < curr.zIndex) {
                    context.layoutElementTreeRoots[i] = next;
                    context.layoutElementTreeRoots[i + 1] = curr;
                }
            }
            sortMax--;
        }

        context.renderCommandsLength = 0;
        dfsLen = 0;

        for (int rootIdx = 0; rootIdx < context.layoutElementTreeRootsLength; rootIdx++) {
            dfsLen = 0;
            LayoutElementTreeRoot root = context.layoutElementTreeRoots[rootIdx];
            LayoutElement rootEl = context.layoutElements[root.layoutElementIndex];

            float rootPositionX = 0f;
            float rootPositionY = 0f;

            FloatingConfigBuilder rootFloatCfg = (FloatingConfigBuilder) rootEl.getConfig(ElementConfigType.FLOATING);
            LayoutElementHashMapItem parentItem = context.getHashMapItem(root.parentId);

            if (rootFloatCfg != null && parentItem != null) {
                Vector2 scratchVector = context.scratchVector;
                computeAttachOffset(parentItem.boundingBox.x, parentItem.boundingBox.y, parentItem.boundingBox.width, parentItem.boundingBox.height, rootEl.dimensions.width, rootEl.dimensions.height, rootFloatCfg, scratchVector);
                rootPositionX = scratchVector.x + rootFloatCfg.offset.x;
                rootPositionY = scratchVector.y + rootFloatCfg.offset.y;
            }

            if (root.clipElementId != 0) {
                LayoutElementHashMapItem clipItem = context.getHashMapItem(root.clipElementId);
                if (clipItem != null) {
                    RenderCommand cmd = nextRenderCommand();
                    if (cmd != null) {
                        cmd.boundingBox.set(clipItem.boundingBox);
                        cmd.id = HashUtil.hashNumber(rootEl.id, rootEl.childrenLength + 10);
                        cmd.zIndex = root.zIndex;
                        cmd.commandType = RenderCommandType.SCISSOR_START;
                    }
                }
            }

            LayoutElementTreeNode rootNode = context.layoutElementTreeNodes[dfsLen];
            rootNode.layoutElement = rootEl;
            rootNode.position.set(rootPositionX, rootPositionY);
            rootNode.nextChildOffset.set(rootEl.layoutConfig.padding.left, rootEl.layoutConfig.padding.top);
            context.treeNodeVisited[dfsLen++] = false;

            while (dfsLen > 0) {
                int nodeDepth = dfsLen - 1;
                LayoutElementTreeNode currentElementTreeNode = context.layoutElementTreeNodes[nodeDepth];
                LayoutElement currentEl = currentElementTreeNode.layoutElement;
                LayoutConfigBuilder layoutCfg = currentEl.layoutConfig;
                Vector2 scrollOffset = context.scratchVector;
                scrollOffset.set(0, 0);

                if (!context.treeNodeVisited[nodeDepth]) {
                    context.treeNodeVisited[nodeDepth] = true;

                    LayoutElementHashMapItem hmItem = context.getHashMapItem(currentEl.id);
                    if (hmItem != null) {
                        hmItem.boundingBox.set(currentElementTreeNode.position.x, currentElementTreeNode.position.y, currentEl.dimensions.width, currentEl.dimensions.height);
                    }

                    ScrollConfigBuilder sc = (ScrollConfigBuilder) currentEl.getConfig(ElementConfigType.SCROLL);
                    if (sc != null) {
                        for (int i = 0; i < context.scrollContainerDatasLength; i++) {
                            if (context.scrollContainerDatas[i].layoutElement == currentEl) {
                                if (sc.horizontal) scrollOffset.x = context.scrollContainerDatas[i].scrollPosition.x;
                                if (sc.vertical) scrollOffset.y = context.scrollContainerDatas[i].scrollPosition.y;
                                if (context.externalScrollHandlingEnabled) {
                                    scrollOffset.set(0, 0);
                                }
                                break;
                            }
                        }

                        RenderCommand cmd = nextRenderCommand();
                        if (cmd != null) {
                            cmd.boundingBox.set(currentElementTreeNode.position.x, currentElementTreeNode.position.y, currentEl.dimensions.width, currentEl.dimensions.height);
                            cmd.id = HashUtil.hashNumber(currentEl.id, currentEl.childrenLength + 10);
                            cmd.commandType = RenderCommandType.SCISSOR_START;
                            cmd.zIndex = root.zIndex;
                        }
                    }

                    emitRenderCommands(currentEl, currentElementTreeNode.position.x, currentElementTreeNode.position.y, root.zIndex);

                    if (!currentEl.isTextElement) {
                        float extraX = 0f, extraY = 0f;
                        if (layoutCfg.direction == LayoutDirection.LEFT_TO_RIGHT) {
                            float contentW = 0f;
                            for (int i = 0; i < currentEl.childrenLength; i++)
                                contentW += context.layoutElements[context.layoutElementChildren[currentEl.childrenStart + i]].dimensions.width;
                            contentW += Math.max(currentEl.childrenLength - 1, 0) * layoutCfg.childGap;
                            float space = currentEl.dimensions.width - (layoutCfg.padding.left + layoutCfg.padding.right) - contentW;
                            if (layoutCfg.alignX == LayoutAlignmentX.CENTER) extraX = space / 2f;
                            else if (layoutCfg.alignX == LayoutAlignmentX.RIGHT) extraX = space;
                            currentElementTreeNode.nextChildOffset.x += Math.max(0, extraX);
                        } else {
                            float contentH = 0f;
                            for (int i = 0; i < currentEl.childrenLength; i++)
                                contentH += context.layoutElements[context.layoutElementChildren[currentEl.childrenStart + i]].dimensions.height;
                            contentH += Math.max(currentEl.childrenLength - 1, 0) * layoutCfg.childGap;
                            float space = currentEl.dimensions.height - (layoutCfg.padding.top + layoutCfg.padding.bottom) - contentH;
                            if (layoutCfg.alignY == LayoutAlignmentY.CENTER) extraY = space / 2f;
                            else if (layoutCfg.alignY == LayoutAlignmentY.BOTTOM) extraY = space;
                            currentElementTreeNode.nextChildOffset.y += Math.max(0, extraY);
                        }
                    }
                } else {
                    boolean closeScissor = currentEl.getConfig(ElementConfigType.SCROLL) != null;
                    ScrollConfigBuilder scConfig = (ScrollConfigBuilder) currentEl.getConfig(ElementConfigType.SCROLL);
                    if (scConfig != null) {
                        for (int i = 0; i < context.scrollContainerDatasLength; i++) {
                            if (context.scrollContainerDatas[i].layoutElement == currentEl) {
                                if (scConfig.horizontal)
                                    scrollOffset.x = context.scrollContainerDatas[i].scrollPosition.x;
                                if (scConfig.vertical)
                                    scrollOffset.y = context.scrollContainerDatas[i].scrollPosition.y;
                                if (context.externalScrollHandlingEnabled) {
                                    scrollOffset.set(0, 0);
                                }
                                break;
                            }
                        }
                    }

                    BorderConfigBuilder borderCfg = (BorderConfigBuilder) currentEl.getConfig(ElementConfigType.BORDER);
                    if (borderCfg != null && borderCfg.width.betweenChildren > 0 && borderCfg.color.a > 0) {
                        SharedConfigBuilder shared = (SharedConfigBuilder) currentEl.getConfig(ElementConfigType.SHARED);
                        float halfGap = layoutCfg.childGap / 2f;
                        Vector2 borderOffset = new Vector2(layoutCfg.padding.left - halfGap, layoutCfg.padding.top - halfGap);

                        if (layoutCfg.direction == LayoutDirection.LEFT_TO_RIGHT) {
                            for (int i = 0; i < currentEl.childrenLength; ++i) {
                                LayoutElement childElement = context.layoutElements[context.layoutElementChildren[currentEl.childrenStart + i]];
                                if (i > 0) {
                                    RenderCommand cmd = nextRenderCommand();
                                    if (cmd != null) {
                                        cmd.commandType = RenderCommandType.RECTANGLE;
                                        cmd.boundingBox.set(currentElementTreeNode.position.x + borderOffset.x + scrollOffset.x, currentElementTreeNode.position.y + scrollOffset.y, borderCfg.width.betweenChildren, currentEl.dimensions.height);
                                        cmd.renderData.backgroundColor = borderCfg.color;
                                        if (shared != null) cmd.userData = shared.userData;
                                        cmd.id = HashUtil.hashNumber(currentEl.id, currentEl.childrenLength + 1 + i);
                                        cmd.zIndex = root.zIndex;
                                    }
                                }
                                borderOffset.x += (childElement.dimensions.width + layoutCfg.childGap);
                            }
                        } else {
                            for (int i = 0; i < currentEl.childrenLength; ++i) {
                                LayoutElement childElement = context.layoutElements[context.layoutElementChildren[currentEl.childrenStart + i]];
                                if (i > 0) {
                                    RenderCommand cmd = nextRenderCommand();
                                    if (cmd != null) {
                                        cmd.commandType = RenderCommandType.RECTANGLE;
                                        cmd.boundingBox.set(currentElementTreeNode.position.x + scrollOffset.x, currentElementTreeNode.position.y + borderOffset.y + scrollOffset.y, currentEl.dimensions.width, borderCfg.width.betweenChildren);
                                        cmd.renderData.backgroundColor = borderCfg.color;
                                        if (shared != null) cmd.userData = shared.userData;
                                        cmd.id = HashUtil.hashNumber(currentEl.id, currentEl.childrenLength + 1 + i);
                                        cmd.zIndex = root.zIndex;
                                    }
                                }
                                borderOffset.y += (childElement.dimensions.height + layoutCfg.childGap);
                            }
                        }
                    }

                    if (closeScissor) {
                        RenderCommand cmd = nextRenderCommand();
                        if (cmd != null) {
                            cmd.id = HashUtil.hashNumber(currentEl.id, currentEl.childrenLength + 11);
                            cmd.commandType = RenderCommandType.SCISSOR_END;
                            cmd.zIndex = root.zIndex;
                        }
                    }
                    dfsLen--;
                    continue;
                }

                if (!currentEl.isTextElement) {
                    int cLen = currentEl.childrenLength;
                    for (int i = 0; i < cLen; i++) {
                        LayoutElement child = context.layoutElements[context.layoutElementChildren[currentEl.childrenStart + i]];

                        if (layoutCfg.direction == LayoutDirection.LEFT_TO_RIGHT) {
                            currentElementTreeNode.nextChildOffset.y = layoutCfg.padding.top;
                            float space = currentEl.dimensions.height - (layoutCfg.padding.top + layoutCfg.padding.bottom) - child.dimensions.height;
                            if (layoutCfg.alignY == LayoutAlignmentY.CENTER)
                                currentElementTreeNode.nextChildOffset.y += space / 2f;
                            else if (layoutCfg.alignY == LayoutAlignmentY.BOTTOM)
                                currentElementTreeNode.nextChildOffset.y += space;
                        } else {
                            currentElementTreeNode.nextChildOffset.x = layoutCfg.padding.left;
                            float space = currentEl.dimensions.width - (layoutCfg.padding.left + layoutCfg.padding.right) - child.dimensions.width;
                            if (layoutCfg.alignX == LayoutAlignmentX.CENTER)
                                currentElementTreeNode.nextChildOffset.x += space / 2f;
                            else if (layoutCfg.alignX == LayoutAlignmentX.RIGHT)
                                currentElementTreeNode.nextChildOffset.x += space;
                        }

                        int newNodeIdx = dfsLen + cLen - 1 - i;
                        LayoutElementTreeNode childNode = context.layoutElementTreeNodes[newNodeIdx];
                        childNode.layoutElement = child;

                        childNode.position.set(currentElementTreeNode.position.x + currentElementTreeNode.nextChildOffset.x + scrollOffset.x, currentElementTreeNode.position.y + currentElementTreeNode.nextChildOffset.y + scrollOffset.y);

                        childNode.nextChildOffset.set(child.layoutConfig.padding.left, child.layoutConfig.padding.top);
                        context.treeNodeVisited[newNodeIdx] = false;

                        if (layoutCfg.direction == LayoutDirection.LEFT_TO_RIGHT)
                            currentElementTreeNode.nextChildOffset.x += child.dimensions.width + layoutCfg.childGap;
                        else
                            currentElementTreeNode.nextChildOffset.y += child.dimensions.height + layoutCfg.childGap;
                    }
                    dfsLen += cLen;
                }
            }

            if (root.clipElementId != 0) {
                RenderCommand cmd = nextRenderCommand();
                if (cmd != null) {
                    cmd.id = HashUtil.hashNumber(rootEl.id, rootEl.childrenLength + 11);
                    cmd.commandType = RenderCommandType.SCISSOR_END;
                }
            }
        }
    }

    private static void sizeContainersAlongAxis(boolean xAxis) {
        ClayJContext context = getContext();
        int bfsLen = 0;
        context.resizableBufferLength = 0;

        for (int rootIdx = 0; rootIdx < context.layoutElementTreeRootsLength; rootIdx++) {
            bfsLen = 0;
            LayoutElementTreeRoot root = context.layoutElementTreeRoots[rootIdx];
            LayoutElement rootEl = context.layoutElements[root.layoutElementIndex];
            context.layoutElementChildrenBuffer[bfsLen++] = root.layoutElementIndex;

            ConfigBuilder fcBuilder = rootEl.getConfig(ElementConfigType.FLOATING);
            if (fcBuilder instanceof FloatingConfigBuilder) {
                LayoutElementHashMapItem parentItem = context.getHashMapItem(((FloatingConfigBuilder) fcBuilder).parentId);
                if (parentItem != null) {
                    if (rootEl.layoutConfig.sizing.width.type == SizingType.GROW)
                        rootEl.dimensions.width = parentItem.layoutElement.dimensions.width;
                    if (rootEl.layoutConfig.sizing.height.type == SizingType.GROW)
                        rootEl.dimensions.height = parentItem.layoutElement.dimensions.height;
                }
            }

            float maxRootW = rootEl.layoutConfig.sizing.width.minMax.max > 0 ? rootEl.layoutConfig.sizing.width.minMax.max : Float.MAX_VALUE;
            rootEl.dimensions.width = Math.max(rootEl.layoutConfig.sizing.width.minMax.min, Math.min(maxRootW, rootEl.dimensions.width));

            float maxRootH = rootEl.layoutConfig.sizing.height.minMax.max > 0 ? rootEl.layoutConfig.sizing.height.minMax.max : Float.MAX_VALUE;
            rootEl.dimensions.height = Math.max(rootEl.layoutConfig.sizing.height.minMax.min, Math.min(maxRootH, rootEl.dimensions.height));

            for (int i = 0; i < bfsLen; i++) {
                int parentIdx = context.layoutElementChildrenBuffer[i];
                LayoutElement parent = context.layoutElements[parentIdx];
                LayoutConfigBuilder parentCfg = parent.layoutConfig;

                int growCount = 0;
                float parentSize = xAxis ? parent.dimensions.width : parent.dimensions.height;
                float parentPadding = xAxis ? (parentCfg.padding.left + parentCfg.padding.right) : (parentCfg.padding.top + parentCfg.padding.bottom);
                float innerContentSize = 0f, totalPaddingAndGaps = parentPadding;
                boolean sizingAlongAxis = (xAxis && parentCfg.direction == LayoutDirection.LEFT_TO_RIGHT) || (!xAxis && parentCfg.direction == LayoutDirection.TOP_TO_BOTTOM);
                context.resizableBufferLength = 0;
                float parentGap = parentCfg.childGap;

                for (int c = 0; c < parent.childrenLength; c++) {
                    int childIdx = context.layoutElementChildren[parent.childrenStart + c];
                    LayoutElement child = context.layoutElements[childIdx];
                    if (!child.isTextElement && child.childrenLength > 0)
                        context.layoutElementChildrenBuffer[bfsLen++] = childIdx;

                    SizingAxis childSizing = xAxis ? child.layoutConfig.sizing.width : child.layoutConfig.sizing.height;
                    float childSize = xAxis ? child.dimensions.width : child.dimensions.height;

                    boolean isTextWithWrap = false;
                    TextConfigBuilder textCfg = (TextConfigBuilder) child.getConfig(ElementConfigType.TEXT);
                    if (child.isTextElement && textCfg != null && textCfg.wrapMode == TextWrapMode.WORDS) {
                        isTextWithWrap = true;
                    }

                    if (childSizing.type != SizingType.PERCENT && childSizing.type != SizingType.FIXED &&
                            (!child.isTextElement || isTextWithWrap)) {
                        context.resizableBuffer[context.resizableBufferLength++] = childIdx;
                    }

                    if (sizingAlongAxis) {
                        if (childSizing.type != SizingType.PERCENT) innerContentSize += childSize;
                        if (childSizing.type == SizingType.GROW) growCount++;
                        if (c > 0) {
                            innerContentSize += parentGap;
                            totalPaddingAndGaps += parentGap;
                        }
                    } else {
                        innerContentSize = Math.max(childSize, innerContentSize);
                    }
                }

                for (int c = 0; c < parent.childrenLength; c++) {
                    LayoutElement child = context.layoutElements[context.layoutElementChildren[parent.childrenStart + c]];
                    SizingAxis childSizing = xAxis ? child.layoutConfig.sizing.width : child.layoutConfig.sizing.height;
                    if (childSizing.type == SizingType.PERCENT) {
                        float sz = (parentSize - totalPaddingAndGaps) * childSizing.percent;
                        if (xAxis) child.dimensions.width = sz;
                        else child.dimensions.height = sz;
                        if (sizingAlongAxis) innerContentSize += sz;
                    }
                }

                if (sizingAlongAxis) {
                    float sizeToDistribute = parentSize - parentPadding - innerContentSize;
                    if (sizeToDistribute < 0) {
                        ConfigBuilder scBuilder = parent.getConfig(ElementConfigType.SCROLL);
                        if (scBuilder instanceof ScrollConfigBuilder) {
                            if ((xAxis && ((ScrollConfigBuilder) scBuilder).horizontal) || (!xAxis && ((ScrollConfigBuilder) scBuilder).vertical)) {

                                for (int s = 0; s < context.scrollContainerDatasLength; s++) {
                                    if (context.scrollContainerDatas[s].elementId == parent.id) {
                                        if (xAxis) context.scrollContainerDatas[s].contentSize.width = innerContentSize;
                                        else context.scrollContainerDatas[s].contentSize.height = innerContentSize;
                                        break;
                                    }
                                }
                                continue;
                            }
                        }
                        while (sizeToDistribute < -EPS && context.resizableBufferLength > 0) {
                            float largest = 0, secondLargest = 0, widthToAdd = sizeToDistribute;
                            for (int r = 0; r < context.resizableBufferLength; r++) {
                                float cSize = xAxis ? context.layoutElements[context.resizableBuffer[r]].dimensions.width : context.layoutElements[context.resizableBuffer[r]].dimensions.height;
                                if (Math.abs(cSize - largest) < EPS) continue;
                                if (cSize > largest) {
                                    secondLargest = largest;
                                    largest = cSize;
                                }
                                if (cSize < largest) {
                                    secondLargest = Math.max(secondLargest, cSize);
                                    widthToAdd = secondLargest - largest;
                                }
                            }
                            widthToAdd = Math.max(widthToAdd, sizeToDistribute / context.resizableBufferLength);
                            for (int r = 0; r < context.resizableBufferLength; r++) {
                                LayoutElement child = context.layoutElements[context.resizableBuffer[r]];
                                float cSize = xAxis ? child.dimensions.width : child.dimensions.height;
                                float cMin = xAxis ? child.minDimensions.width : child.minDimensions.height;
                                if (Math.abs(cSize - largest) < EPS) {
                                    cSize += widthToAdd;
                                    if (cSize <= cMin) {
                                        cSize = cMin;
                                        ArrayUtil.removeSwapback(context.resizableBuffer, context.resizableBufferLength, r);
                                        context.resizableBufferLength--;
                                        r--;
                                    }
                                    if (xAxis) {
                                        sizeToDistribute -= cSize - child.dimensions.width;
                                        child.dimensions.width = cSize;
                                    } else {
                                        sizeToDistribute -= cSize - child.dimensions.height;
                                        child.dimensions.height = cSize;
                                    }
                                }
                            }
                        }
                    } else if (sizeToDistribute > 0 && growCount > 0) {
                        for (int r = 0; r < context.resizableBufferLength; r++) {
                            SizingType st = xAxis ? context.layoutElements[context.resizableBuffer[r]].layoutConfig.sizing.width.type : context.layoutElements[context.resizableBuffer[r]].layoutConfig.sizing.height.type;
                            if (st != SizingType.GROW) {
                                ArrayUtil.removeSwapback(context.resizableBuffer, context.resizableBufferLength, r);
                                context.resizableBufferLength--;
                                r--;
                            }
                        }
                        while (sizeToDistribute > EPS && context.resizableBufferLength > 0) {
                            float smallest = Float.MAX_VALUE, secondSmallest = Float.MAX_VALUE, widthToAdd = sizeToDistribute;
                            for (int r = 0; r < context.resizableBufferLength; r++) {
                                float cSize = xAxis ? context.layoutElements[context.resizableBuffer[r]].dimensions.width : context.layoutElements[context.resizableBuffer[r]].dimensions.height;
                                if (Math.abs(cSize - smallest) < EPS) continue;
                                if (cSize < smallest) {
                                    secondSmallest = smallest;
                                    smallest = cSize;
                                }
                                if (cSize > smallest) {
                                    secondSmallest = Math.min(secondSmallest, cSize);
                                    widthToAdd = secondSmallest - smallest;
                                }
                            }
                            widthToAdd = Math.min(widthToAdd, sizeToDistribute / context.resizableBufferLength);
                            for (int r = 0; r < context.resizableBufferLength; r++) {
                                LayoutElement child = context.layoutElements[context.resizableBuffer[r]];
                                float cSize = xAxis ? child.dimensions.width : child.dimensions.height;
                                float cMax = xAxis ? child.layoutConfig.sizing.width.minMax.max : child.layoutConfig.sizing.height.minMax.max;
                                if (cMax <= 0) cMax = Float.MAX_VALUE;
                                if (Math.abs(cSize - smallest) < EPS) {
                                    cSize += widthToAdd;
                                    if (cSize >= cMax) {
                                        cSize = cMax;
                                        ArrayUtil.removeSwapback(context.resizableBuffer, context.resizableBufferLength, r);
                                        context.resizableBufferLength--;
                                        r--;
                                    }
                                    if (xAxis) {
                                        sizeToDistribute -= cSize - child.dimensions.width;
                                        child.dimensions.width = cSize;
                                    } else {
                                        sizeToDistribute -= cSize - child.dimensions.height;
                                        child.dimensions.height = cSize;
                                    }
                                }
                            }
                        }
                    }
                } else {
                    float maxSize = parentSize - parentPadding;
                    ConfigBuilder scBuilder = parent.getConfig(ElementConfigType.SCROLL);
                    if (scBuilder instanceof ScrollConfigBuilder) {
                        if ((xAxis && ((ScrollConfigBuilder) scBuilder).horizontal) || (!xAxis && ((ScrollConfigBuilder) scBuilder).vertical)) {
                            maxSize = Math.max(maxSize, innerContentSize);

                            for (int s = 0; s < context.scrollContainerDatasLength; s++) {
                                if (context.scrollContainerDatas[s].elementId == parent.id) {
                                    if (xAxis) context.scrollContainerDatas[s].contentSize.width = innerContentSize;
                                    else context.scrollContainerDatas[s].contentSize.height = innerContentSize;
                                    break;
                                }
                            }
                        }
                    }
                    for (int r = 0; r < context.resizableBufferLength; r++) {
                        LayoutElement child = context.layoutElements[context.resizableBuffer[r]];
                        SizingAxis sa = xAxis ? child.layoutConfig.sizing.width : child.layoutConfig.sizing.height;
                        if (!xAxis && child.getConfig(ElementConfigType.IMAGE) != null) continue;
                        float cSize = xAxis ? child.dimensions.width : child.dimensions.height;
                        if (sa.type == SizingType.FIT) cSize = Math.max(sa.minMax.min, Math.min(cSize, maxSize));
                        else if (sa.type == SizingType.GROW)
                            cSize = Math.min(maxSize, sa.minMax.max > 0 ? sa.minMax.max : Float.MAX_VALUE);
                        if (xAxis) child.dimensions.width = cSize;
                        else child.dimensions.height = cSize;
                    }
                }
            }
        }
    }

    private static void wrapTextElements() {
        ClayJContext context = getContext();
        for (int ti = 0; ti < context.textElementDataLength; ti++) {
            TextElementData ted = context.textElementData[ti];
            LayoutElement el = context.layoutElements[ted.elementIndex];
            TextConfigBuilder textCfg = (TextConfigBuilder) el.getConfig(ElementConfigType.TEXT);
            if (textCfg == null || ted.text == null) continue;

            ted.wrappedLinesStart = context.wrappedTextLinesLength;
            ted.wrappedLinesLength = 0;
            float containerWidth = el.dimensions.width;
            float lineHeight = textCfg.lineHeight > 0 ? textCfg.lineHeight : ted.preferredDimensions.height;

            MeasureTextCacheItem mtci = measureTextCached(ted.text, textCfg);

            if (!mtci.containsNewlines && ted.preferredDimensions.width <= containerWidth) {
                appendWrappedLine(ted, 0, ted.text.length(), ted.preferredDimensions.width, lineHeight);
                el.dimensions.height = lineHeight;
                continue;
            }

            float lineWidth = 0f;
            int lineChars = 0, lineStartOffset = 0;
            int wordIdx = mtci.measureWordsStartIndex;

            context.measureTextFunction.measure(" ", 0, 1, textCfg, context.scratchDimensions);
            float spaceWidth = context.scratchDimensions.width;

            while (wordIdx != -1) {
                if (context.wrappedTextLinesLength > context.wrappedTextLines.length - 1) break;
                MeasuredWord word = context.measuredWords[wordIdx];

                if (lineChars == 0 && lineWidth + word.width > containerWidth) {
                    appendWrappedLine(ted, word.startOffset, word.length, word.width, lineHeight);
                    wordIdx = word.next;
                    lineStartOffset = word.startOffset + word.length;
                } else if (word.length == 0 || lineWidth + word.width > containerWidth) {
                    boolean finalSpace = ted.text.charAt(Math.max(lineStartOffset + lineChars - 1, 0)) == ' ';
                    appendWrappedLine(ted, lineStartOffset, lineChars - (finalSpace ? 1 : 0), lineWidth + (finalSpace ? -spaceWidth : 0), lineHeight);
                    if (lineChars == 0 || word.length == 0) wordIdx = word.next;
                    lineWidth = 0;
                    lineChars = 0;
                    lineStartOffset = word.startOffset;
                } else {
                    lineWidth += word.width + textCfg.letterSpacing;
                    lineChars += word.length;
                    wordIdx = word.next;
                }
            }
            if (lineChars > 0)
                appendWrappedLine(ted, lineStartOffset, lineChars, lineWidth - textCfg.letterSpacing, lineHeight);
            el.dimensions.height = lineHeight * ted.wrappedLinesLength;
        }
    }

    private static void appendWrappedLine(TextElementData ted, int lineStart, int lineLength, float width, float height) {
        ClayJContext context = getContext();
        if (context.wrappedTextLinesLength >= context.wrappedTextLines.length) return;
        WrappedTextLine wl = context.wrappedTextLines[context.wrappedTextLinesLength++];
        wl.lineStart = lineStart;
        wl.lineLength = lineLength;
        wl.dimensions.set(width, height);
        ted.wrappedLinesLength++;
    }

    private static void fixImageAspectRatios() {
        ClayJContext context = getContext();
        for (int i = 0; i < context.imageElementPointersLength; i++) {
            LayoutElement el = context.layoutElements[context.imageElementPointers[i]];
            ImageConfigBuilder img = (ImageConfigBuilder) el.getConfig(ElementConfigType.IMAGE);
            if (img == null || img.sourceDimensions.width == 0 || img.sourceDimensions.height == 0) continue;
            float aspect = img.sourceDimensions.width / img.sourceDimensions.height;
            if (el.dimensions.width == 0f && el.dimensions.height != 0f)
                el.dimensions.width = el.dimensions.height * aspect;
            else if (el.dimensions.width != 0f && el.dimensions.height == 0f)
                el.dimensions.height = el.dimensions.width / aspect;
        }
    }

    private static void emitRenderCommands(LayoutElement el, float elX, float elY, short zIndex) {
        ClayJContext context = getContext();
        SharedConfigBuilder shared = (SharedConfigBuilder) el.getConfig(ElementConfigType.SHARED);
        if (shared != null && shared.backgroundColor != null) {
            RenderCommand cmd = nextRenderCommand();
            if (cmd != null) {
                cmd.commandType = RenderCommandType.RECTANGLE;
                cmd.boundingBox.set(elX, elY, el.dimensions.width, el.dimensions.height);
                cmd.renderData.backgroundColor = shared.backgroundColor;
                cmd.renderData.cornerRadius = shared.cornerRadius;
                cmd.userData = shared.userData;
                cmd.id = el.id;
                cmd.zIndex = zIndex;
            }
        }

        if (el.isTextElement && el.textElementDataIndex >= 0) {
            TextElementData ted = context.textElementData[el.textElementDataIndex];
            TextConfigBuilder textCfg = (TextConfigBuilder) el.getConfig(ElementConfigType.TEXT);
            if (textCfg != null) {
                float lineY = elY;
                for (int li = 0; li < ted.wrappedLinesLength; li++) {
                    WrappedTextLine wl = context.wrappedTextLines[ted.wrappedLinesStart + li];
                    RenderCommand cmd = nextRenderCommand();
                    if (cmd == null) break;

                    float alignOffset = 0;
                    if (textCfg.textAlignment == TextAlignment.CENTER)
                        alignOffset = (el.dimensions.width - wl.dimensions.width) / 2f;
                    else if (textCfg.textAlignment == TextAlignment.RIGHT)
                        alignOffset = (el.dimensions.width - wl.dimensions.width);

                    cmd.commandType = RenderCommandType.TEXT;
                    cmd.boundingBox.set(elX + alignOffset, lineY, wl.dimensions.width, wl.dimensions.height);
                    cmd.renderData.text = ted.text;
                    cmd.renderData.textStart = wl.lineStart;
                    cmd.renderData.textLength = wl.lineLength;
                    cmd.renderData.textColor = textCfg.textColor;
                    cmd.renderData.fontId = textCfg.fontId;
                    cmd.renderData.fontSize = textCfg.fontSize;
                    cmd.renderData.letterSpacing = textCfg.letterSpacing;
                    cmd.renderData.lineHeight = textCfg.lineHeight;
                    cmd.id = el.id;
                    cmd.zIndex = zIndex;
                    lineY += textCfg.lineHeight > 0 ? textCfg.lineHeight : ted.preferredDimensions.height;
                }
            }
        }

        ImageConfigBuilder imgCfg = (ImageConfigBuilder) el.getConfig(ElementConfigType.IMAGE);
        if (imgCfg != null && imgCfg.imageData != null) {
            RenderCommand cmd = nextRenderCommand();
            if (cmd != null) {
                cmd.commandType = RenderCommandType.IMAGE;
                cmd.boundingBox.set(elX, elY, el.dimensions.width, el.dimensions.height);
                cmd.renderData.imageData = imgCfg.imageData;
                cmd.renderData.sourceDimensions.set(imgCfg.sourceDimensions);
                if (shared != null && shared.cornerRadius != null) cmd.renderData.cornerRadius = shared.cornerRadius;
                cmd.id = el.id;
                cmd.zIndex = zIndex;
            }
        }

        CustomConfigBuilder customCfg = (CustomConfigBuilder) el.getConfig(ElementConfigType.CUSTOM);
        if (customCfg != null && customCfg.customData != null) {
            RenderCommand cmd = nextRenderCommand();
            if (cmd != null) {
                cmd.commandType = RenderCommandType.CUSTOM;
                cmd.boundingBox.set(elX, elY, el.dimensions.width, el.dimensions.height);
                cmd.renderData.customData = customCfg.customData;
                if (shared != null && shared.backgroundColor != null)
                    cmd.renderData.backgroundColor = shared.backgroundColor;
                if (shared != null && shared.cornerRadius != null) cmd.renderData.cornerRadius = shared.cornerRadius;
                cmd.id = el.id;
                cmd.zIndex = zIndex;
            }
        }

        BorderConfigBuilder borderCfg = (BorderConfigBuilder) el.getConfig(ElementConfigType.BORDER);
        if (borderCfg != null) {
            RenderCommand cmd = nextRenderCommand();
            if (cmd != null) {
                cmd.commandType = RenderCommandType.BORDER;
                cmd.boundingBox.set(elX, elY, el.dimensions.width, el.dimensions.height);
                cmd.renderData.borderColor = borderCfg.color;
                cmd.renderData.borderWidth = borderCfg.width;
                if (shared != null && shared.cornerRadius != null) cmd.renderData.cornerRadius = shared.cornerRadius;
                cmd.id = HashUtil.hashNumber(el.id, el.childrenLength);
                cmd.zIndex = zIndex;
            }
        }
    }

    private static void computeAttachOffset(float px, float py, float pw, float ph, float fw, float fh, FloatingConfigBuilder fc, Vector2 outOffset) {
        float x = px, y = py;
        switch (fc.attachParent) {
            case CENTER_TOP:
            case CENTER_CENTER:
            case CENTER_BOTTOM:
                x = px + pw * 0.5f;
                break;
            case RIGHT_TOP:
            case RIGHT_CENTER:
            case RIGHT_BOTTOM:
                x = px + pw;
                break;
        }
        switch (fc.attachParent) {
            case LEFT_CENTER:
            case CENTER_CENTER:
            case RIGHT_CENTER:
                y = py + ph * 0.5f;
                break;
            case LEFT_BOTTOM:
            case CENTER_BOTTOM:
            case RIGHT_BOTTOM:
                y = py + ph;
                break;
        }
        switch (fc.attachElement) {
            case CENTER_TOP:
            case CENTER_CENTER:
            case CENTER_BOTTOM:
                x -= fw * 0.5f;
                break;
            case RIGHT_TOP:
            case RIGHT_CENTER:
            case RIGHT_BOTTOM:
                x -= fw;
                break;
        }
        switch (fc.attachElement) {
            case LEFT_CENTER:
            case CENTER_CENTER:
            case RIGHT_CENTER:
                y -= fh * 0.5f;
                break;
            case LEFT_BOTTOM:
            case CENTER_BOTTOM:
            case RIGHT_BOTTOM:
                y -= fh;
                break;
        }
        outOffset.set(x, y);
    }

    private static void generateAnonId(LayoutElement el, boolean isOnStack) {
        ClayJContext context = getContext();
        int parentId = 0;
        int siblingIndex = context.layoutElementTreeRootsLength;

        int stackOffset = isOnStack ? 2 : 1;

        if (context.openLayoutElementStackLength >= stackOffset) {
            int parentIdx = context.openLayoutElementStack[context.openLayoutElementStackLength - stackOffset];
            LayoutElement parent = context.layoutElements[parentIdx];
            parentId = parent.id;
            siblingIndex = parent.childrenLength - 1;
        }

        ElementId eid = context.transientIds.take();
        HashUtil.hashNumber(siblingIndex, parentId, eid);
        el.id = eid.id;
        context.addHashMapItem(eid, el, context.layoutElementsLength - 1, 0);
    }

    private static RenderCommand nextRenderCommand() {
        ClayJContext context = getContext();
        if (context.renderCommandsLength >= context.renderCommands.length) {
            if (!context.maxRenderCommandsExceeded && context.errorHandler != null) {
                context.maxRenderCommandsExceeded = true;
                context.errorHandler.handleError(ClayJError.ELEMENTS_CAPACITY_EXCEEDED);
            }
            return null;
        }
        RenderCommand cmd = context.renderCommands[context.renderCommandsLength++];
        cmd.reset();
        return cmd;
    }
}