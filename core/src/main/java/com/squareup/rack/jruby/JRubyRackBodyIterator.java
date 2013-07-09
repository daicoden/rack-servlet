/*
 * Copyright (C) 2013 Square, Inc.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package com.squareup.rack.jruby;

import com.google.common.collect.AbstractIterator;
import org.jruby.RubyEnumerator;
import org.jruby.RubyString;
import org.jruby.exceptions.RaiseException;
import org.jruby.runtime.ThreadContext;
import org.jruby.runtime.builtin.IRubyObject;

/**
 * Adapts a (RubyObject) enumerable into Java-space.
 */
public class JRubyRackBodyIterator extends AbstractIterator<byte[]> {
  private final IRubyObject body;
  private final ThreadContext threadContext;
  private final RubyEnumerator enumerator;

  public JRubyRackBodyIterator(IRubyObject body) {
    this.body = body;
    this.threadContext = body.getRuntime().getThreadService().getCurrentContext();
    this.enumerator = (RubyEnumerator) body.callMethod(threadContext, "to_enum");
  }

  @Override protected byte[] computeNext() {
    try {
      return ((RubyString) enumerator.callMethod(threadContext, "next")).getBytes();
    } catch (RaiseException e) {
      close();
      return endOfData();
    }
  }

  private void close() {
    if (body.respondsTo("close")) {
      body.callMethod(threadContext, "close");
    }
  }
}
