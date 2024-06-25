package org.pentaho.di.pan;

public interface CommandExecutorResult {

  public static enum Result {
    SUCCESS( 0, "Success" ),
    ERRORS_INVALID_PROJECT( 10, "The project path is invalid, please provide a valid one" );

    private static int code;
    private static String description;

    Result( int code, String description ) {
      setCode( code );
      setDescription( description );
    }

    public int getCode() {
      return code;
    }

    private void setCode( int code ) {
      this.code = code;
    }

    public String getDescription() {
      return description;
    }

    private void setDescription( String description ) {
      this.description = description;
    }
  }
  public void setResult( CommandExecutorResult.Result result );

  public CommandExecutorResult.Result getResult();
}
