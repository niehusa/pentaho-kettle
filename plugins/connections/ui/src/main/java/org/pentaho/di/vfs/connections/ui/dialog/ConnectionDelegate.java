/*! ******************************************************************************
 *
 * Pentaho Data Integration
 *
 * Copyright (C) 2002-2024 by Hitachi Vantara : http://www.pentaho.com
 *
 *******************************************************************************
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with
 * the License. You may obtain a copy of the License at
 *
 *    http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 *
 ******************************************************************************/

package org.pentaho.di.vfs.connections.ui.dialog;

import org.eclipse.swt.SWT;
import org.pentaho.di.base.AbstractMeta;
import org.pentaho.di.connections.ConnectionDetails;
import org.pentaho.di.connections.ConnectionManager;
import org.pentaho.di.connections.ui.dialog.ConnectionDeleteDialog;
import org.pentaho.di.connections.ui.dialog.ConnectionOverwriteDialog;
import org.pentaho.di.connections.ui.tree.ConnectionFolderProvider;
import org.pentaho.di.core.bowl.Bowl;
import org.pentaho.di.core.bowl.DefaultBowl;
import org.pentaho.di.core.EngineMetaInterface;
import org.pentaho.di.core.exception.KettleException;
import org.pentaho.di.i18n.BaseMessages;
import org.pentaho.di.ui.core.dialog.ErrorDialog;
import org.pentaho.di.ui.core.widget.tree.LeveledTreeNode;
import org.pentaho.di.ui.spoon.Spoon;

import java.util.function.Supplier;

/**
 * Created by bmorrise on 2/4/19.
 */
public class ConnectionDelegate {

  private static final Class<?> PKG = ConnectionDelegate.class;
  private Supplier<Spoon> spoonSupplier = Spoon::getInstance;

  private static final int WIDTH = 630;
  private static final int HEIGHT = 630;
  private static ConnectionDelegate instance;

  private ConnectionDelegate() {
    // no-op
  }

  public static ConnectionDelegate getInstance() {
    if ( null == instance ) {
      instance = new ConnectionDelegate();
    }
    return instance;
  }

  public void openDialog() {
    try {
      Spoon spoon = spoonSupplier.get();
      Bowl bowl = spoon.getBowl();
      ConnectionDialog connectionDialog = new ConnectionDialog( spoon.getShell(), WIDTH, HEIGHT,
                                                                bowl.getManager( ConnectionManager.class ) );
      connectionDialog.open( BaseMessages.getString( PKG, "ConnectionDialog.dialog.new.title" ) );
    } catch ( KettleException e ) {
      showError( e );
    }
  }

  public void openDialog( String name, LeveledTreeNode.LEVEL level ) {
    try {
      Spoon spoon = spoonSupplier.get();
      Bowl bowl = getBowl( spoon, level );
      ConnectionDialog connectionDialog = new ConnectionDialog( spoon.getShell(), WIDTH, HEIGHT,
                                                                bowl.getManager( ConnectionManager.class ) );
      connectionDialog.open( BaseMessages.getString( PKG, "ConnectionDialog.dialog.edit.title" ), name );
    } catch ( KettleException e ) {
      showError( e );
    }
  }

  public void delete( String name, LeveledTreeNode.LEVEL level ) {
    try {
      ConnectionDeleteDialog connectionDeleteDialog = new ConnectionDeleteDialog( spoonSupplier.get().getShell() );
      if ( connectionDeleteDialog.open( name ) == SWT.YES ) {
        Spoon spoon = spoonSupplier.get();
        Bowl bowl = getBowl( spoon, level );
        ConnectionManager connectionManager = bowl.getManager( ConnectionManager.class );
        connectionManager.delete( name );

        spoonSupplier.get().getShell().getDisplay().asyncExec( () -> spoonSupplier.get().refreshTree(
          ConnectionFolderProvider.STRING_VFS_CONNECTIONS ) );
        EngineMetaInterface engineMetaInterface = spoonSupplier.get().getActiveMeta();
        if ( engineMetaInterface instanceof AbstractMeta ) {
          ( (AbstractMeta) engineMetaInterface ).setChanged();
        }
      }
    } catch ( KettleException e ) {
      showError( e );
    }
  }

  public void copyToGlobal( String name ) {
    moveCopy( name, spoonSupplier.get().getBowl(), DefaultBowl.getInstance(), false );
  }

  public void copyToProject( String name ) {
    moveCopy( name, DefaultBowl.getInstance(), spoonSupplier.get().getBowl(), false );
  }

  public void moveToGlobal( String name ) {
    moveCopy( name, spoonSupplier.get().getBowl(), DefaultBowl.getInstance(), true );
  }

  public void moveToProject( String name ) {
    moveCopy( name, DefaultBowl.getInstance(), spoonSupplier.get().getBowl(), true );
  }

  private void moveCopy( String name, Bowl sourceBowl, Bowl targetBowl, boolean deleteSource ) {
    try {
      ConnectionDetails targetConnection = targetBowl.getManager( ConnectionManager.class ).getConnectionDetails( name );
      if ( targetConnection != null ) {
        ConnectionOverwriteDialog connectionOverwriteDialog =
          new ConnectionOverwriteDialog( spoonSupplier.get().getShell() );
        if ( !( connectionOverwriteDialog.open( name ) == SWT.YES ) ) {
          return;
        }
      }

      ConnectionDetails details = sourceBowl.getManager( ConnectionManager.class ).getConnectionDetails( name );
      if ( details == null ) {
        throw new KettleException( "Connection not found: " + name );
      }
      targetBowl.getManager( ConnectionManager.class ).save( details );
      if ( deleteSource ) {
        sourceBowl.getManager( ConnectionManager.class ).delete( name );
      }
      spoonSupplier.get().getShell().getDisplay().asyncExec( () -> spoonSupplier.get().refreshTree(
        ConnectionFolderProvider.STRING_VFS_CONNECTIONS ) );
    } catch ( KettleException e ) {
      showError( e );
    }
  }

  private Bowl getBowl( Spoon spoon, LeveledTreeNode.LEVEL level ) {
    if ( level == LeveledTreeNode.LEVEL.PROJECT ) {
      return spoon.getBowl();
    } else {
      return DefaultBowl.getInstance();
    }
  }

  private void showError( Exception e ) {
    new ErrorDialog( spoonSupplier.get().getShell(),
                     BaseMessages.getString( PKG, "Spoon.ErrorDialog.Title" ),
                     BaseMessages.getString( PKG, "Spoon.ErrorDialog.ErrorFetchingVFSConnections" ),
                     e
                     );
  }
}

