/*
 *(c) Copyright QNX Software Systems Ltd. 2002.
 * All Rights Reserved.
 * 
 */
package org.eclipse.cdt.debug.internal.ui.actions;

import org.eclipse.cdt.debug.core.CDebugCorePlugin;
import org.eclipse.cdt.debug.core.CDebugModel;
import org.eclipse.cdt.debug.core.model.IExecFileInfo;
import org.eclipse.cdt.debug.core.model.IGlobalVariable;
import org.eclipse.cdt.debug.ui.CDebugUIPlugin;
import org.eclipse.core.runtime.IPath;
import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.MultiStatus;
import org.eclipse.debug.core.DebugException;
import org.eclipse.debug.core.DebugPlugin;
import org.eclipse.debug.core.model.IDebugElement;
import org.eclipse.debug.core.model.IDebugTarget;
import org.eclipse.debug.core.model.IExpression;
import org.eclipse.debug.ui.DebugUITools;
import org.eclipse.debug.ui.IDebugUIConstants;
import org.eclipse.jface.action.IAction;
import org.eclipse.jface.viewers.ISelection;
import org.eclipse.jface.viewers.IStructuredContentProvider;
import org.eclipse.jface.viewers.IStructuredSelection;
import org.eclipse.jface.viewers.LabelProvider;
import org.eclipse.jface.viewers.StructuredSelection;
import org.eclipse.jface.viewers.Viewer;
import org.eclipse.jface.window.Window;
import org.eclipse.swt.custom.BusyIndicator;
import org.eclipse.swt.widgets.Display;
import org.eclipse.ui.IPartListener;
import org.eclipse.ui.ISelectionListener;
import org.eclipse.ui.IViewActionDelegate;
import org.eclipse.ui.IViewPart;
import org.eclipse.ui.IWorkbenchPart;
import org.eclipse.ui.IWorkbenchWindow;
import org.eclipse.ui.actions.ActionDelegate;
import org.eclipse.ui.dialogs.ListSelectionDialog;

/**
 * Enter type comment.
 * 
 * @since: Nov 4, 2002
 */
public class AddGlobalsActionDelegate extends ActionDelegate
									  implements IViewActionDelegate, 
												 ISelectionListener,
												 IPartListener
{
	protected class Global
	{
		private String fName;
		private IPath fPath;

		/**
		 * Constructor for Global.
		 */
		public Global( String name, IPath path )
		{
			fName = name;
			fPath = path;
		}

		public String getName()
		{
			return fName;
		}
		
		public IPath getPath()
		{
			return fPath;
		}
	}

	private Global[] fGlobals;
	private IViewPart fView = null;
	private IAction fAction;
	private IStructuredSelection fSelection;
	private IStatus fStatus = null;

	/**
	 * Constructor for AddGlobalsActionDelegate.
	 */
	public AddGlobalsActionDelegate()
	{
		super();
	}

	/* (non-Javadoc)
	 * @see org.eclipse.ui.IViewActionDelegate#init(IViewPart)
	 */
	public void init( IViewPart view )
	{
		fView = view;
		view.getSite().getPage().addPartListener( this );
		view.getSite().getPage().addSelectionListener( IDebugUIConstants.ID_DEBUG_VIEW, this );
	}

	/* (non-Javadoc)
	 * @see org.eclipse.ui.ISelectionListener#selectionChanged(IWorkbenchPart, ISelection)
	 */
	public void selectionChanged( IWorkbenchPart part, ISelection selection )
	{
		if ( part != null && part.getSite().getId().equals( IDebugUIConstants.ID_DEBUG_VIEW ) )
		{
			if ( selection instanceof IStructuredSelection )
			{
				setSelection( (IStructuredSelection)selection );
			}
			else
			{
				setSelection( null );
			}
			update( getAction() );
		}
	}

	/* (non-Javadoc)
	 * @see org.eclipse.ui.IActionDelegate#run(IAction)
	 */
	public void run( IAction action )
	{
		final IStructuredSelection selection = getSelection();
		if ( selection != null && selection.size() != 1 )
			return;
		BusyIndicator.showWhile( Display.getCurrent(), 
								 new Runnable() 
								 	 {
										 public void run() 
										 {
											 try 
											 {
												 doAction( selection.getFirstElement() );
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
				CDebugUIPlugin.errorDialog( getErrorDialogMessage(), getStatus() );
			} 
			else 
			{
				CDebugUIPlugin.log( getStatus() );
			}
		}		
	}

	/* (non-Javadoc)
	 * @see org.eclipse.ui.IActionDelegate#selectionChanged(IAction, ISelection)
	 */
	public void selectionChanged( IAction action, ISelection selection )
	{
		setAction( action );
		if ( getView() != null )
		{
			update( action );
		}
	}

	protected void update( IAction action )
	{
		if ( action != null )
		{
			action.setEnabled( getEnableStateForSelection( getSelection() ) );
		}
	}

	/* (non-Javadoc)
	 * @see org.eclipse.ui.IPartListener#partActivated(IWorkbenchPart)
	 */
	public void partActivated( IWorkbenchPart part )
	{
	}

	/* (non-Javadoc)
	 * @see org.eclipse.ui.IPartListener#partBroughtToTop(IWorkbenchPart)
	 */
	public void partBroughtToTop( IWorkbenchPart part )
	{
	}

	/* (non-Javadoc)
	 * @see org.eclipse.ui.IPartListener#partClosed(IWorkbenchPart)
	 */
	public void partClosed( IWorkbenchPart part )
	{
		if ( part.equals( getView() ) )
		{
			dispose();
		}
	}

	/* (non-Javadoc)
	 * @see org.eclipse.ui.IPartListener#partDeactivated(IWorkbenchPart)
	 */
	public void partDeactivated( IWorkbenchPart part )
	{
	}

	/* (non-Javadoc)
	 * @see org.eclipse.ui.IPartListener#partOpened(IWorkbenchPart)
	 */
	public void partOpened( IWorkbenchPart part )
	{
	}
	
	protected IViewPart getView()
	{
		return fView;
	}

	protected void setView( IViewPart viewPart )
	{
		fView = viewPart;
	}
	
	protected void setAction( IAction action )
	{
		fAction = action;
	}

	protected IAction getAction()
	{
		return fAction;
	}

	private void setSelection( IStructuredSelection selection )
	{
		fSelection = selection;
	}

	private IStructuredSelection getSelection()
	{
		return fSelection;
	}

	public void dispose()
	{
		if ( getView() != null ) 
		{
			getView().getViewSite().getPage().removeSelectionListener( IDebugUIConstants.ID_DEBUG_VIEW, this );
			getView().getViewSite().getPage().removePartListener( this );
		}	
	}

	protected boolean getEnableStateForSelection( IStructuredSelection selection )
	{
		if ( selection == null || selection.size() != 1 )
		{
			return false;
		}
		Object element = selection.getFirstElement();
		return ( element != null && 
				 element instanceof IDebugElement && 
				 ((IDebugElement)element).getDebugTarget().getAdapter( IExecFileInfo.class ) != null );
	}

	private ListSelectionDialog createDialog()
	{
		return new ListSelectionDialog( getView().getSite().getShell(), 
										fGlobals,
										new IStructuredContentProvider()
											{
												public void inputChanged( Viewer viewer, Object oldInput, Object newInput ) 
												{
												}
										 
												public void dispose() 
												{
												}
										
												public Object[] getElements( Object parent ) 
												{
													 return getGlobals();
												}
											},
										new LabelProvider() 
										{
											public String getText( Object element ) 
											{
												if ( element instanceof Global )
												{
													String path = ""; //$NON-NLS-1$
													if ( ((Global)element).getPath() != null )
													{
														path = ((Global)element).getPath().toString();
														int index = path.lastIndexOf( '/' );
													 	if ( index != -1 )
													 		path = path.substring( index + 1 );
													}
													return ( path.length() > 0 ? ( '\'' + path + "\'::" ) : "" ) + ((Global)element).getName(); //$NON-NLS-1$ //$NON-NLS-2$
												}
												return null;				
											}
										},
										CDebugUIPlugin.getResourceString("internal.ui.actions.AddGlobalsActionDelegate.Select_Variables") ); //$NON-NLS-1$
	}
	
	protected Global[] getGlobals()
	{
		return fGlobals;
	}

	protected void doAction( Object element ) throws DebugException
	{
		if ( getView() == null )
			return;
		if ( element != null && element instanceof IDebugElement )
		{
			IExecFileInfo info = (IExecFileInfo)((IDebugElement)element).getDebugTarget().getAdapter( IExecFileInfo.class );
			if ( info != null )
			{
				IGlobalVariable[] globalVars = info.getGlobals();
				fGlobals = new Global[globalVars.length];
				for ( int i = 0; i < globalVars.length; ++i )
				{
					fGlobals[i] = new Global( globalVars[i].getName(), globalVars[i].getPath() );
				}
				ListSelectionDialog dlg = createDialog();
				if ( dlg.open() == Window.OK )
				{
					MultiStatus ms = new MultiStatus( CDebugCorePlugin.getUniqueIdentifier(), DebugException.REQUEST_FAILED, getStatusMessage(), null ); 
					Object[] selections = dlg.getResult();
					for ( int i = 0; i < selections.length; ++i )
					{
						try
						{
							createExpression( ((IDebugElement)element).getDebugTarget(), (Global)selections[i]  );
						}
						catch( DebugException e )
						{
							ms.merge( e.getStatus() );
						}
					}
					if ( !ms.isOK() )
					{
						throw new DebugException( ms );
					}
				}
			}
		}
	}

	protected String getStatusMessage()
	{
		return CDebugUIPlugin.getResourceString("internal.ui.actions.AddGlobalsActionDelegate.Exceptions_occurred_attempting_to_add_global_variables."); //$NON-NLS-1$
	}

	/**
	 * @see AbstractDebugActionDelegate#getErrorDialogMessage()
	 */
	protected String getErrorDialogMessage()
	{
		return CDebugUIPlugin.getResourceString("internal.ui.actions.AddGlobalsActionDelegate.Add_global_variables_failed"); //$NON-NLS-1$
	}
	
	protected void setStatus( IStatus status )
	{
		fStatus = status;
	}
	
	protected IStatus getStatus()
	{
		return fStatus;
	}

	private void createExpression( IDebugTarget target, Global global ) throws DebugException
	{
		IExpression expression = CDebugModel.createExpressionForGlobalVariable( target, global.getPath(), global.getName() );
		DebugPlugin.getDefault().getExpressionManager().addExpression( expression );
	}

	/* (non-Javadoc)
	 * @see org.eclipse.ui.IActionDelegate2#init(org.eclipse.jface.action.IAction)
	 */
	public void init( IAction action )
	{
		super.init( action );
		Object element = DebugUITools.getDebugContext();
		setSelection( (element != null ) ? new StructuredSelection( element ) : new StructuredSelection() );
		update( action );
	}
}
