/*! ******************************************************************************
 *
 * Pentaho Data Integration
 *
 * Copyright (C) 2018-2024 by Hitachi Vantara : http://www.pentaho.com
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

package org.pentaho.di.ui.spoon.tree;

import org.eclipse.swt.graphics.Image;
import org.pentaho.di.base.AbstractMeta;
import org.pentaho.di.core.util.Utils;
import org.pentaho.di.ui.core.widget.tree.TreeNode;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.ConcurrentHashMap;
import java.util.Optional;
import java.util.Set;

/**
 * Created by bmorrise on 7/9/18.
 */
public class RootNode extends TreeNode {

  private List<TreeFolderProvider> treeFolderProviders = new ArrayList<>();
  Set<String> updates = ConcurrentHashMap.newKeySet();

  public RootNode( String label, Image image, boolean expanded ) {
    super( label, image, expanded );
  }

  public void addProvider( TreeFolderProvider treeFolderProvider ) {
    treeFolderProviders.add( treeFolderProvider );
    treeFolderProvider.create( Optional.empty(), this );
  }

  public void addProviders( List<TreeFolderProvider> treeFolderProviders ) {
    this.treeFolderProviders.addAll( treeFolderProviders );
    treeFolderProviders.forEach( tfp -> tfp.create( Optional.empty(), this ) );
  }

  public void checkUpdate( Optional<AbstractMeta> abstractMeta, String filter ) {
    for ( TreeFolderProvider treeFolderProvider : treeFolderProviders ) {
      TreeNode childTreeNode = getChildTreeNode( this, treeFolderProvider.getTitle() );
      if ( childTreeNode != null ) {
        treeFolderProvider.checkUpdate( abstractMeta, childTreeNode, filter );
        if ( !Utils.isEmpty( filter ) ) {
          childTreeNode.setExpanded( true );
        }
      }
    }
  }

  private TreeNode getChildTreeNode( TreeNode treeNode, String label ) {
    for ( TreeNode childTreeNode : treeNode.getChildren() ) {
      if ( childTreeNode.getLabel().equals( label ) ) {
        return childTreeNode;
      }
    }
    return null;
  }

  public String getNameByType( Class clazz ) {
    TreeFolderProvider treeFolderProvider = treeFolderProviders.stream()
            .filter( treeFolderProvider1 -> treeFolderProvider1.getType().equals( clazz ) )
            .findFirst()
            .orElse( null );
    if ( treeFolderProvider != null ) {
      return treeFolderProvider.getTitle();
    }
    return null;
  }

  public void updateAll() {
    for ( TreeFolderProvider tfp : treeFolderProviders ) {
      update( tfp.getTitle() );
    }
  }

  public void update( String name ) {
    updates.add( name );
  }

  public void clearUpdates() {
    updates.clear();
  }

  public boolean shouldUpdate( String name ) {
    return updates.contains( name );
  }
}
