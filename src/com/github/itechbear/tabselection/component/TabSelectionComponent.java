package com.github.itechbear.tabselection.component;

import com.github.itechbear.tabselection.handler.TabSelectionHandler;
import com.intellij.codeInsight.lookup.Lookup;
import com.intellij.codeInsight.lookup.impl.actions.ChooseItemAction;
import com.intellij.codeInsight.template.impl.editorActions.ExpandLiveTemplateByTabAction;
import com.intellij.openapi.actionSystem.ActionManager;
import com.intellij.openapi.actionSystem.IdeActions;
import com.intellij.openapi.components.ProjectComponent;
import org.jetbrains.annotations.NotNull;

/**
 * Created with IntelliJ IDEA.
 * User: HD
 * Date: 13-8-26
 * Time: 下午12:28
 * To change this template use File | Settings | File Templates.
 */
public class TabSelectionComponent implements ProjectComponent {
    public TabSelectionComponent() {
    }

    public void initComponent() {
        // TODO: insert component initialization logic here
        // ActionManager.getInstance().addAnActionListener(new com.example.component.TabSelectionListener());

        // disable live template expansion key.
        ExpandLiveTemplateByTabAction expandLiveTemplateByTabAction = (ExpandLiveTemplateByTabAction) ActionManager.getInstance().getAction(IdeActions.ACTION_EXPAND_LIVE_TEMPLATE_BY_TAB);
        ActionManager.getInstance().unregisterAction(IdeActions.ACTION_EXPAND_LIVE_TEMPLATE_BY_TAB);

        ChooseItemAction.Replacing replacingAction = (ChooseItemAction.Replacing) ActionManager.getInstance().getAction(IdeActions.ACTION_CHOOSE_LOOKUP_ITEM_REPLACE);
        replacingAction.setupHandler(new TabSelectionHandler(false, Lookup.REPLACE_SELECT_CHAR));
    }

    public void disposeComponent() {
        // TODO: insert component disposal logic here
    }

    @NotNull
    public String getComponentName() {
        return "TabSelectionComponent";
    }

    @Override
    public void projectOpened() {

    }

    @Override
    public void projectClosed() {

    }
}
