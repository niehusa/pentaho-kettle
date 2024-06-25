package org.pentaho.di.pan;

import org.pentaho.di.core.exception.KettleException;
import org.pentaho.di.core.logging.LogChannelInterface;

import java.util.List;

public interface CommandLineOptionProvider {
  /**
   * Prepare additional commandline option.
   *
   * @param options Existing commandline options
   * @param param   Additional commandline option.
   */
  void prepareAdditionalCommandlineOption( List<CommandLineOption> options, StringBuilder param );

  /**
   * Handle additional parameter
   *
   * @param log
   * @param param
   * @return Error string in case of error, or empty string if succeeded/
   */
  CommandExecutorResult handleParameter( LogChannelInterface log, String param ) throws KettleException;
}
