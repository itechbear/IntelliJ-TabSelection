package com.github.itechbear.tabselection.handler;

import com.intellij.codeStyle.CodeStyleFacade;
import com.intellij.openapi.actionSystem.CommonDataKeys;
import com.intellij.openapi.actionSystem.DataContext;
import com.intellij.openapi.command.CommandProcessor;
import com.intellij.openapi.editor.*;
import com.intellij.openapi.editor.actionSystem.EditorActionManager;
import com.intellij.openapi.editor.actionSystem.EditorWriteActionHandler;
import com.intellij.openapi.editor.ex.EditorEx;
import com.intellij.openapi.fileEditor.FileDocumentManager;
import com.intellij.openapi.fileTypes.FileType;
import com.intellij.openapi.project.Project;
import com.intellij.openapi.util.Key;
import com.intellij.openapi.util.TextRange;
import com.intellij.openapi.util.text.StringUtil;
import com.intellij.openapi.vfs.VirtualFile;
import com.intellij.util.ui.MacUIUtil;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.Arrays;
import java.util.HashSet;
import java.util.Set;

/**
 * Created by HD on 2014/9/8.
 */
public class SkipParenthesisHandler extends EditorWriteActionHandler {
    public SkipParenthesisHandler() {
        super(true);
    }
    private static Set<String> SKIP_CHARS = new HashSet<String>(Arrays.asList(";", ")", "]", "}"));

    public void executeWriteAction(Editor editor, @Nullable Caret caret, DataContext dataContext) {
        if(caret == null) {
            caret = editor.getCaretModel().getPrimaryCaret();
        }

        CommandProcessor.getInstance().setCurrentCommandGroupId(Key.create("EditGroup"));
        CommandProcessor.getInstance().setCurrentCommandName(EditorBundle.message("typing.command.name", new Object[0]));
        Project project = CommonDataKeys.PROJECT.getData(dataContext);
        if (shouldSkip(editor, caret, project)) {
            skipParanthesis(editor, caret, project);
        } else {
            insertTabAtCaret(editor, caret, project);
        }
    }

    public boolean isEnabled(Editor editor, DataContext dataContext) {
        return !editor.isOneLineMode() && !((EditorEx)editor).isEmbeddedIntoDialogWrapper() && !editor.isViewer();
    }

    private static boolean shouldSkip(Editor editor, @NotNull Caret caret, Project project) {
        if (caret.hasSelection()) {
            return false;
        }

        int offset = editor.getCaretModel().getOffset();
        Document document = editor.getDocument();
        TextRange textRange = new TextRange(offset, offset + 1);
        String string = document.getText(textRange);

        return SKIP_CHARS.contains(string);
    }

    private static void skipParanthesis(Editor editor, @NotNull Caret caret, Project project) {
        int offset = editor.getCaretModel().getOffset();
        while (shouldSkip(editor, caret, project)) {
            offset++;
            editor.getCaretModel().moveToOffset(offset);
        }
    }

    private static void insertTabAtCaret(Editor editor, @NotNull Caret caret, Project project) {
        MacUIUtil.hideCursor();
        int columnNumber;
        if(caret.hasSelection()) {
            columnNumber = editor.visualToLogicalPosition(caret.getSelectionStartPosition()).column;
        } else {
            columnNumber = editor.getCaretModel().getLogicalPosition().column;
        }

        CodeStyleFacade settings = CodeStyleFacade.getInstance(project);
        Document doc = editor.getDocument();
        VirtualFile vFile = FileDocumentManager.getInstance().getFile(doc);
        FileType fileType = vFile == null?null:vFile.getFileType();
        int tabSize = settings.getIndentSize(fileType);
        int spacesToAddCount = tabSize - columnNumber % Math.max(1, tabSize);
        boolean useTab = editor.getSettings().isUseTabCharacter(project);
        CharSequence chars = doc.getCharsSequence();
        if(useTab && settings.isSmartTabs(fileType)) {
            int e = editor.getCaretModel().getOffset();

            while(e > 0) {
                --e;
                if(chars.charAt(e) != 9) {
                    if(chars.charAt(e) != 10) {
                        useTab = false;
                    }
                    break;
                }
            }
        }

        doc.startGuardedBlockChecking();

        try {
            if(useTab) {
                EditorModificationUtil.typeInStringAtCaretHonorBlockSelection(editor, "\t", false);
            } else {
                EditorModificationUtil.typeInStringAtCaretHonorBlockSelection(editor, StringUtil.repeatSymbol(' ', spacesToAddCount), false);
            }
        } catch (ReadOnlyFragmentModificationException var16) {
            EditorActionManager.getInstance().getReadonlyFragmentModificationHandler(doc).handle(var16);
        } finally {
            doc.stopGuardedBlockChecking();
        }
    }
}
