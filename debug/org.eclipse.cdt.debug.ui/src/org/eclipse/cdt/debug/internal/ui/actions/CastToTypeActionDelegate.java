/*
 *(c) Copyright QNX Software Systems Ltd. 2002.
 * All Rights Reserved.
 * 
 */

package org.eclipse.cdt.debug.internal.ui.actions;

import org.eclipse.cdt.debug.core.model.ICastToType;
import org.eclipse.cdt.debug.internal.ui.CDebugImages;
import org.eclipse.cdt.debug.ui.CDebugUIPlugin;
import org.eclipse.core.runtime.IStatus;
import org.eclipse.debug.core.DebugException;
import org.eclipse.debug.ui.IDebugView;
import org.eclipse.jface.action.IAction;
import org.eclipse.jface.dialogs.IInputValidator;
import org.eclipse.jface.dialogs.InputDialog;
import org.eclipse.jface.viewers.ISelection;
import org.eclipse.jface.viewers.ISelectionProvider;
import org.eclipse.jface.viewers.IStructuredSelection;
import org.eclipse.jface.viewers.StructuredSelection;
import org.eclipse.jface.window.Window;
import org.eclipse.swt.custom.BusyIndicator;
import org.eclipse.swt.widgets.Display;
import org.eclipse.swt.widgets.Shell;
import org.eclipse.ui.IObjectActionDelegate;
import org.eclipse.ui.IWorkbenchPart;
import org.eclipse.ui.IWorkbenchWindow;
import org.eclipse.ui.actions.ActionDelegate;

/**
 * Enter type comment.
 * 
 * @since Mar 7, 2003
 */
public class CastToTypeActionDelegate extends ActionDelegate
									  implements IObjectActionDelegate
{
	static protected class CastToTypeInputValidator implements IInputValidator
	{
		public CastToTypeInputValidator()
		{
		}

		public String isValid( String newText )
		{
			if ( newText.trim().length() == 0 )
			{
				return CDebugUIPlugin.getResourceString("internal.ui.actions.CastToTypeActionDelegate.Type_field_must_not_be_empty"); //$NON-NLS-1$
			}
			return null;
		}

	}

	protected class CastToTypeDialog extends InputDialog
	{
		public CastToTypeDialog( Shell parentShell, String initialValue )
		{
			super( parentShell, 
				   CDebugUIPlugin.getResourceString("internal.ui.actions.CastToTypeActionDelegate.Cast_To_Type"), 
				   CDebugUIPlugin.getResourceString("internal.ui.actions.CastToTypeActionDelegate.Enter_type"), 
				   initialValue, new CastToTypeInputValidator() ); //$NON-NLS-1$ //$NON-NLS-2$
		}

		/* (non-Javadoc)
		 * @see org.eclipse.jface.window.Window#configureShell(org.eclipse.swt.widgets.Shell)
		 */
		protected void configureShell( Shell shell )
		{
			super.configureShell( shell );
			shell.setImage( CDebugImages.get( CDebugImages.IMG_LCL_CAST_TO_TYPE ) );
		}

	}

	private ICastToType fCastToType = null;
	private IStatus fStatus = null;
	private IWorkbenchPart fTargetPart = null;

	public CastToTypeActionDelegate()
	{
		super();
	}

	/* (non-Javadoc)
	 * @see org.eclipse.ui.IObjectActionDelegate#setActivePart(org.eclipse.jface.action.IAction, org.eclipse.ui.IWorkbenchPart)
	 */
	public void setActivePart( IAction action, IWorkbenchPart targetPart )
	{
		fTargetPart = targetPart;
	}

	/* (non-Javadoc)
	 * @see org.eclipse.ui.IActionDelegate#run(org.eclipse.jface.action.IAction)
	 */
	public void run( IAction action )
	{
		if ( getCastToType() == null )
			return;
		BusyIndicator.showWhile( Display.getCurrent(), 
								 new Runnable() 
									 {
										 public void run() 
										 {
											 try 
											 {
												 doAction( getCastToType() );
												 setStatus( null );
											 } 
											 catch( DebugException e ) 
											 {
												setStatus( e.getStatus() );
											 }
										 }
									 } );
		if ( getStatus() != null && !getStatus().isOK() ) 
		{
			IWorkbenchWindow window= CDebugUIPlugin.getActiveWorkbenchWindow();
			if ( window != null ) 
			{
				CDebugUIPlugin.errorDialog( CDebugUIPlugin.getResourceString("internal.ui.actions.CastToTypeActionDelegate.Unable_to_cast_to_type."), getStatus() ); //$NON-NLS-1$
			} 
			else 
			{
				CDebugUIPlugin.log( getStatus() );
			}
		}		
	}

	/* (non-Javadoc)
	 * @see org.eclipse.ui.IActionDelegate#selectionChanged(org.eclipse.jface.action.IAction, org.eclipse.jface.viewers.ISelection)
	 */
	public void selectionChanged( IAction action, ISelection selection )
	{
		if ( selection instanceof IStructuredSelection )
		{
			Object element = ((IStructuredSelection)selection).getFirstElement();
			if ( element instanceof ICastToType )
			{
				boolean enabled = ((ICastToType)element).supportsCasting();
				action.setEnabled( enabled );
				if ( enabled )
				{
					setCastToType( (ICastToType)element );
					return;
				}
			}
		}
		action.setEnabled( false );
		setCastToType( null );
	}

	protected ICastToType getCastToType()
	{
		return fCastToType;
	}

	protected void setCastToType( ICastToType castToType )
	{
		fCastToType = castToType;
	}

	public IStatus getStatus()
	{
		return fStatus;
	}

	public void setStatus( IStatus status )
	{
		fStatus = status;
	}
	
	protected void doAction( ICastToType castToType ) throws DebugException
	{
		String currentType = castToType.getCurrentType().trim();
		CastToTypeDialog dialog = new CastToTypeDialog( CDebugUIPlugin.getActiveWorkbenchShell(), currentType );
		if ( dialog.open() == Window.OK )
		{
			String newType = dialog.getValue().trim();
			castToType.cast( newType );
			if ( getSelectionProvider() != null )
				getSelectionProvider().setSelection( new StructuredSelection( castToType ) );
		}
	}

	private ISelectionProvider getSelectionProvider()
	{
		return ( fTargetPart instanceof IDebugView ) ? ((IDebugView)fTargetPart).getViewer() : null;
	}
}
