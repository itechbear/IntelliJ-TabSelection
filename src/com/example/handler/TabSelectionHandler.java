package com.example.handler;

import com.intellij.codeInsight.completion.CodeCompletionFeatures;
import com.intellij.codeInsight.lookup.Lookup;
import com.intellij.codeInsight.lookup.LookupManager;
import com.intellij.codeInsight.lookup.impl.CompletionPreview;
import com.intellij.codeInsight.lookup.impl.LookupImpl;
import com.intellij.codeInsight.lookup.impl.actions.ChooseItemAction;
import com.intellij.codeInsight.template.impl.TemplateSettings;
import com.intellij.featureStatistics.FeatureUsageTracker;
import com.intellij.openapi.actionSystem.DataContext;
import com.intellij.openapi.editor.Editor;
import com.intellij.openapi.editor.actionSystem.EditorActionHandler;
import org.jetbrains.annotations.NotNull;

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

    @Override
    public void execute(@NotNull final Editor editor, final DataContext dataContext) {
        final LookupImpl lookup = (LookupImpl) LookupManager.getActiveLookup(editor);
        if (lookup == null) {
            throw new AssertionError("The last lookup disposed at: " + LookupImpl.getLastLookupDisposeTrace() + "\n-----------------------\n");
        }

        if (finishingChar == Lookup.NORMAL_SELECT_CHAR) {
            if (!lookup.isFocused()) {
                FeatureUsageTracker.getInstance().triggerFeatureUsed(CodeCompletionFeatures.EDITING_COMPLETION_CONTROL_ENTER);
            }
        } else if (finishingChar == Lookup.COMPLETE_STATEMENT_SELECT_CHAR) {
            FeatureUsageTracker.getInstance().triggerFeatureUsed(CodeCompletionFeatures.EDITING_COMPLETION_FINISH_BY_SMART_ENTER);
        } else if (finishingChar == Lookup.REPLACE_SELECT_CHAR) {
            // Messages.showMessageDialog("tab key!", "title", Messages.getInformationIcon());
            FeatureUsageTracker.getInstance().triggerFeatureUsed(CodeCompletionFeatures.EDITING_COMPLETION_REPLACE);

            int count = lookup.getList().getModel().getSize();
            if (count == 0) {
                return;
            }
            int index = (lookup.getList().getSelectedIndex() + 1) % count;
            lookup.setFocused(true);
            lookup.getList().setSelectedIndex(index);
            lookup.getList().ensureIndexIsVisible(index);
            return;
        } else if (finishingChar == '.') {
            FeatureUsageTracker.getInstance().triggerFeatureUsed(CodeCompletionFeatures.EDITING_COMPLETION_FINISH_BY_CONTROL_DOT);
        }

        lookup.finishLookup(finishingChar);
    }

    @Override
    public boolean isEnabled(Editor editor, DataContext dataContext) {
        LookupImpl lookup = (LookupImpl) LookupManager.getActiveLookup(editor);
        if (lookup == null) return false;
        if (!lookup.isAvailableToUser()) return false;
        if (focusedOnly && !CompletionPreview.hasPreview(lookup) && !lookup.isFocused()) return false;
        if (finishingChar == Lookup.NORMAL_SELECT_CHAR && ChooseItemAction.hasTemplatePrefix(lookup, TemplateSettings.ENTER_CHAR) ||
                finishingChar == Lookup.REPLACE_SELECT_CHAR && ChooseItemAction.hasTemplatePrefix(lookup, TemplateSettings.TAB_CHAR)) {
            return false;
        }
        if (finishingChar == Lookup.REPLACE_SELECT_CHAR) {
            if (lookup.isFocused()) {
                return true;
            }
            return !lookup.getItems().isEmpty();
        }

        return true;
    }

}