package jsbeautifier.popup.actions;

import java.io.BufferedReader;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.io.StringWriter;
import java.io.Writer;

import javax.script.ScriptEngine;
import javax.script.ScriptEngineManager;

import org.eclipse.jface.action.IAction;
import org.eclipse.jface.dialogs.MessageDialog;
import org.eclipse.jface.dialogs.ProgressMonitorDialog;
import org.eclipse.jface.text.IDocument;
import org.eclipse.jface.viewers.ISelection;
import org.eclipse.swt.widgets.Shell;
import org.eclipse.ui.IEditorActionDelegate;
import org.eclipse.ui.IEditorPart;
import org.eclipse.ui.IObjectActionDelegate;
import org.eclipse.ui.IWorkbenchPart;
import org.eclipse.ui.editors.text.TextEditor;
import org.eclipse.ui.texteditor.AbstractTextEditor;
import org.eclipse.ui.texteditor.IDocumentProvider;
import org.eclipse.vjet.eclipse.internal.ui.editor.JavaScriptEditor;

public class NewAction implements IObjectActionDelegate, IEditorActionDelegate {

	private String text;
	private Shell shell;
	private IDocument doc;

	/**
	 * Constructor for Action1.
	 */
	public NewAction() {
		super();
	}

	/**
	 * @see IObjectActionDelegate#setActivePart(IAction, IWorkbenchPart)
	 */
	public void setActivePart(IAction action, IWorkbenchPart targetPart) {
		shell = targetPart.getSite().getShell();
	}

	/**
	 * @see IActionDelegate#run(IAction)
	 */
	public void run(IAction action) {
		final ProgressMonitorDialog dialog = new ProgressMonitorDialog(shell);
		dialog.open();

		ScriptEngineManager factory = new ScriptEngineManager();
		ScriptEngine engine = factory.getEngineByName("JavaScript");
		try {
			InputStream is = getClass().getResourceAsStream("beautify.js");
			InputStreamReader isr = new InputStreamReader(is);
			BufferedReader reader = new BufferedReader(isr);


			StringBuffer fileData = new StringBuffer();
			char[] buf = new char[1024];

			int r = 0;
			while((r = reader.read(buf)) != -1){
				fileData.append(buf, 0 , r);
			}

			reader.close();
			isr.close();
			is.close();

			fileData.append("\r\n\r\n");
			text = doc.get();
			text = text.replace("\\", "\\\\").replace("\"", "\\\"").replace("\'", "\\\'").replace("\n", "\\n").replace("\r", "\\r");
			fileData.append("js_beautify('"+text+"', { 'indent_size': 1, 'indent_char': '\t' });");

			String beautifiedString = (String)engine.eval(fileData.toString());
			doc.set(beautifiedString);
			dialog.close();


		} catch (Exception e){
			e.printStackTrace();
			dialog.close();
			MessageDialog.openError(shell, "Error", getStackTrace(e));
		}
	}

	public static String getStackTrace(Throwable aThrowable) {
		final Writer result = new StringWriter();
		final PrintWriter printWriter = new PrintWriter(result);
		aThrowable.printStackTrace(printWriter);
		return result.toString();
	}

	/**
	 * @see IActionDelegate#selectionChanged(IAction, ISelection)
	 */
	public void selectionChanged(IAction action, ISelection selection) {
	}

	@Override
	public void setActiveEditor(IAction arg0, IEditorPart arg1) {

		if(arg1 instanceof JavaScriptEditor){
			JavaScriptEditor editor = (JavaScriptEditor)arg1;
			IDocumentProvider documentProvider = editor.getDocumentProvider();
			doc = documentProvider.getDocument(editor.getEditorInput());
		} else if(arg1 instanceof AbstractTextEditor) {
			AbstractTextEditor editor = (AbstractTextEditor)arg1;
			IDocumentProvider documentProvider = editor.getDocumentProvider();
			doc = documentProvider.getDocument(editor.getEditorInput());
		}
	}
}
