package com.github.itechbear.tabselection.handler;

import com.intellij.codeInsight.lookup.Lookup;
import com.intellij.codeInsight.lookup.LookupManager;
import com.intellij.codeInsight.lookup.impl.LookupImpl;
import com.intellij.openapi.actionSystem.DataContext;
import com.intellij.openapi.editor.Caret;
import com.intellij.openapi.editor.Editor;
import com.intellij.openapi.editor.actionSystem.EditorActionHandler;
import org.jetbrains.annotations.Nullable;

import javax.swing.*;

/**
 * Created with IntelliJ IDEA.
 * User: HD
 * Date: 13-8-26
 * Time: 下午12:15
 * To change this template use File | Settings | File Templates.
 */
public class TabSelectionHandler extends EditorActionHandler {
    final boolean focusedOnly;
    final char finishingChar;

    public TabSelectionHandler(boolean focusedOnly, char finishingChar) {
        this.focusedOnly = focusedOnly;
        this.finishingChar = finishingChar;
    }

//    @Override
//    public void execute(@NotNull final Editor editor, final DataContext dataContext) {
//        final LookupImpl lookup = (LookupImpl) LookupManager.getActiveLookup(editor);
//        if (lookup == null) {
//            throw new AssertionError("The last lookup disposed at: " + LookupImpl.getLastLookupDisposeTrace() + "\n-----------------------\n");
//        }
//
//        if (finishingChar == Lookup.REPLACE_SELECT_CHAR) {
//            downSelect(lookup);
//        } else {
//            lookup.finishLookup(finishingChar);
//        }
//    }

    @Override
    protected void doExecute(Editor editor, @Nullable Caret caret, DataContext dataContext) {
        // super.doExecute(editor, caret, dataContext);
        final LookupImpl lookup = (LookupImpl) LookupManager.getActiveLookup(editor);
        if (lookup == null) {
            throw new AssertionError("The last lookup disposed at: " + LookupImpl.getLastLookupDisposeTrace() + "\n-----------------------\n");
        }

        if (finishingChar == Lookup.REPLACE_SELECT_CHAR) {
            downSelect(lookup);
        } else {
            lookup.finishLookup(finishingChar);
        }
    }

    @Override
    public boolean isEnabled(Editor editor, DataContext dataContext) {
        LookupImpl lookup = (LookupImpl) LookupManager.getActiveLookup(editor);
        if (lookup == null) {
            return false;
        }
        if (!lookup.isAvailableToUser()) {
            return false;
        }
        if (focusedOnly && lookup.getFocusDegree() == LookupImpl.FocusDegree.UNFOCUSED) {
            return false;
        }
//        if (finishingChar == Lookup.NORMAL_SELECT_CHAR && ChooseItemAction.hasTemplatePrefix(lookup, TemplateSettings.ENTER_CHAR) ||
//                finishingChar == Lookup.REPLACE_SELECT_CHAR && ChooseItemAction.hasTemplatePrefix(lookup, TemplateSettings.TAB_CHAR)) {
//            return false;
//        }
        if (finishingChar == Lookup.REPLACE_SELECT_CHAR) {
            return !lookup.getItems().isEmpty();
        }

        return true;
    }

    private static void downSelect(LookupImpl lookup) {
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
        lookup.setFocusDegree(LookupImpl.FocusDegree.FOCUSED);
        jList.setSelectedIndex(index);
        int visible = index;
        if (visible > jList.getLastVisibleIndex() - 1) {
            visible += jList.getVisibleRowCount() - 2;
            if (visible >= count) {
                visible = count - 1;
            }
        }
        jList.ensureIndexIsVisible(visible);
    }

}