package com.github.itechbear.tabselection.component;

import com.github.itechbear.tabselection.handler.TabSelectionHandler;
import com.intellij.codeInsight.lookup.Lookup;
import com.intellij.codeInsight.lookup.impl.actions.ChooseItemAction;
import com.intellij.ide.AppLifecycleListener;
import com.intellij.openapi.actionSystem.ActionManager;
import com.intellij.openapi.actionSystem.IdeActions;
import org.jetbrains.annotations.NotNull;

import java.util.List;

/**
 * Created with IntelliJ IDEA.
 * User: HD
 * Date: 13-8-26
 * Time: 12:28AM
 * To change this template use File | Settings | File Templates.
 */
public class TabSelectionComponent implements AppLifecycleListener {
    @Override
    public void appFrameCreated(@NotNull List<String> commandLineArgs) {
        ChooseItemAction.Replacing replacingAction = (ChooseItemAction.Replacing) ActionManager.getInstance().getAction(IdeActions.ACTION_CHOOSE_LOOKUP_ITEM_REPLACE);
        replacingAction.setupHandler(new TabSelectionHandler(false, Lookup.REPLACE_SELECT_CHAR));
    }
}
