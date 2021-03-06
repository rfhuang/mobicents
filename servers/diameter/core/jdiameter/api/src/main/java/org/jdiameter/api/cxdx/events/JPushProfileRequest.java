package org.jdiameter.api.cxdx.events;

import org.jdiameter.api.app.AppRequestEvent;

/**
 * Start time:13:45:50 2009-08-17<br>
 * Project: diameter-parent<br>
 * 
 * @author <a href="mailto:baranowb@gmail.com"> Bartosz Baranowski </a>
 * @author <a href="mailto:brainslog@gmail.com"> Alexandre Mendonca </a>
 */
public interface JPushProfileRequest extends AppRequestEvent {

  public static final String _SHORT_NAME = "PPR";
  public static final String _LONG_NAME = "Push-Profile-Request";
  public static final int code = 305;
}
