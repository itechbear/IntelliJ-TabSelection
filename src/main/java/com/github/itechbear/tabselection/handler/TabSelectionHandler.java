package com.github.itechbear.tabselection.handler;

import com.intellij.codeInsight.lookup.Lookup;
import com.intellij.codeInsight.lookup.LookupFocusDegree;
import com.intellij.codeInsight.lookup.LookupManager;
import com.intellij.codeInsight.lookup.impl.LookupImpl;
import com.intellij.codeInsight.lookup.impl.actions.ChooseItemAction;
import com.intellij.codeInsight.template.impl.editorActions.ExpandLiveTemplateCustomAction;
import com.intellij.openapi.actionSystem.DataContext;
import com.intellij.openapi.editor.Caret;
import com.intellij.openapi.editor.Editor;
import com.intellij.openapi.editor.actionSystem.EditorActionHandler;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import javax.swing.*;
import java.awt.*;

/**
 * Created with IntelliJ IDEA.
 * User: HD
 * Date: 13-8-26
 * Time: 12:15AM
 * To change this template use File | Settings | File Templates.
 */
public class TabSelectionHandler extends EditorActionHandler {
    final boolean focusedOnly;
    final char finishingChar;

    public TabSelectionHandler(boolean focusedOnly, char finishingChar) {
        this.focusedOnly = focusedOnly;
        this.finishingChar = finishingChar;
    }

    @Override
    protected void doExecute(@NotNull Editor editor, @Nullable Caret caret, DataContext dataContext) {
        // super.doExecute(editor, caret, dataContext);
        final LookupImpl lookup = (LookupImpl) LookupManager.getActiveLookup(editor);
        assert lookup != null;

        if ((finishingChar == Lookup.NORMAL_SELECT_CHAR || finishingChar == Lookup.REPLACE_SELECT_CHAR) &&
                ChooseItemAction.hasTemplatePrefix(lookup, finishingChar)) {
            lookup.hideLookup(true);

            ExpandLiveTemplateCustomAction.createExpandTemplateHandler(finishingChar).execute(editor, null, dataContext);

            return;
        }

        if (finishingChar == Lookup.REPLACE_SELECT_CHAR) {
            downSelect(lookup);
        } else {
            lookup.finishLookup(finishingChar);
        }
    }

    @Override
    public boolean isEnabledForCaret(@NotNull Editor editor, @NotNull Caret caret, DataContext dataContext) {
        LookupImpl lookup = (LookupImpl)LookupManager.getActiveLookup(editor);
        if (lookup == null) return false;
        if (!lookup.isAvailableToUser()) return false;
        if (focusedOnly && lookup.getLookupFocusDegree() == LookupFocusDegree.UNFOCUSED) return false;
        if (finishingChar == Lookup.REPLACE_SELECT_CHAR) {
            return !lookup.getItems().isEmpty();
        }

        return true;
    }

    private static void downSelect(@NotNull LookupImpl lookup) {
        JList jList = lookup.getList();
        if (jList == null) {
            return;
        }
        ListModel listModel = jList.getModel();
        if (listModel == null) {
            return;
        }
        int count = listModel.getSize();
        if (count == 0) {
            return;
        }
        int index = (jList.getSelectedIndex() + 1) % count;
        lookup.setLookupFocusDegree(LookupFocusDegree.FOCUSED);
        jList.setSelectedIndex(index);
        int last = index + jList.getVisibleRowCount() / 2;
        Rectangle rectangle = jList.getCellBounds(index, last);
        if (rectangle == null) {
            return;
        }
        jList.scrollRectToVisible(rectangle);
    }

}