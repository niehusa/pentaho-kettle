package org.pentaho.di.pan;

import org.pentaho.di.core.exception.KettleException;
import org.pentaho.di.core.logging.LogChannelInterface;

import java.util.List;

public interface CommandLineOptionProvider {
  void prepareAdditionalCommandlineOption( List<CommandLineOption> options, StringBuilder param );

  void validateExecute( LogChannelInterface log, String param ) throws KettleException;
}
