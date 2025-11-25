/*
 * This software is subject to the terms of the Eclipse Public License v1.0
 * Agreement, available at the following URL:
 * http://www.eclipse.org/legal/epl-v10.html.
 * You must accept the terms of that agreement to use this software.
 *
 * Copyright (C) 2016-2017 Hitachi Vantara and others
 * All Rights Reserved.
 *
 * ---- All changes after Fork in 2023 ------------------------
 * 
 * Project: Eclipse daanse
 * 
 * Copyright (c) 2023 Contributors to the Eclipse Foundation.
 *
 * This program and the accompanying materials are made
 * available under the terms of the Eclipse Public License 2.0
 * which is available at https://www.eclipse.org/legal/epl-2.0/
 *
 * SPDX-License-Identifier: EPL-2.0
 *
 * Contributors after Fork in 2023:
 *   SmartCity Jena - initial
 *   Stefan Bischof (bipolis.org) - initial
 */

package org.eclipse.daanse.olap.util;

import org.eclipse.daanse.olap.api.ConfigConstants;
import org.eclipse.daanse.olap.api.Execution;

/**
 * Encapsulates cancel and timeouts checks
 *
 * @author Yury_Bakhmutski
 * @since Jan 18, 2016
 */
public class CancellationChecker {

    private CancellationChecker() {
        // constructor
    }

    public static void checkCancelOrTimeout(
      int currentIteration, Execution execution)
  {
    checkCancelOrTimeout((long) currentIteration, execution);
  }

  public static void checkCancelOrTimeout(
      long currentIteration, Execution execution)
  {
    if (execution != null && execution.getDaanseStatement() != null) {
      int checkCancelOrTimeoutInterval = execution.getDaanseStatement().getDaanseConnection().getContext()
              .getConfigValue(ConfigConstants.CHECK_CANCEL_OR_TIMEOUT_INTERVAL, ConfigConstants.CHECK_CANCEL_OR_TIMEOUT_INTERVAL_DEFAULT_VALUE, Integer.class);
      synchronized (execution) {
        if (checkCancelOrTimeoutInterval > 0
            && currentIteration % checkCancelOrTimeoutInterval == 0)
        {
          execution.checkCancelOrTimeout();
        }
      }
    }
  }
}
