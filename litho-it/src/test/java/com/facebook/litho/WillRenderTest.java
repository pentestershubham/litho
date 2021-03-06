/*
 * Copyright (c) 2017-present, Facebook, Inc.
 * All rights reserved.
 *
 * This source code is licensed under the BSD-style license found in the
 * LICENSE file in the root directory of this source tree. An additional grant
 * of patent rights can be found in the PATENTS file in the same directory.
 */

package com.facebook.litho;

import static com.facebook.litho.testing.assertj.LithoAssertions.assertThat;
import static org.robolectric.RuntimeEnvironment.application;

import android.view.View;
import com.facebook.litho.testing.testrunner.ComponentsTestRunner;
import com.facebook.litho.testing.util.InlineLayoutSpec;
import com.facebook.litho.testing.util.InlineLayoutWithSizeSpec;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.ExpectedException;
import org.junit.runner.RunWith;

@RunWith(ComponentsTestRunner.class)
public class WillRenderTest {

  private final InlineLayoutSpec mNullSpec =
      new InlineLayoutSpec() {

        @Override
        protected Component onCreateLayout(ComponentContext c) {
          return null;
        }
      };

  private final InlineLayoutSpec mNonNullSpec =
      new InlineLayoutSpec() {

        @Override
        protected Component onCreateLayout(ComponentContext c) {
          return Row.create(c).build();
        }
      };

  private final InlineLayoutWithSizeSpec mLayoutWithSizeSpec =
      new InlineLayoutWithSizeSpec() {

        @Override
        protected Component onCreateLayoutWithSizeSpec(
            ComponentContext c, int widthSpec, int heightSpec) {
          return Row.create(c)
              .widthDip(View.MeasureSpec.getSize(widthSpec))
              .heightDip(View.MeasureSpec.getSize(heightSpec))
              .build();
        }
      };

  @Rule public ExpectedException mExpectedException = ExpectedException.none();

  @Test
  public void testWillRenderForComponentThatReturnsNull() {
    ComponentContext c = new ComponentContext(application);
    assertThat(c, Wrapper.create(c).delegate(mNullSpec).build()).wontRender();
  }

  @Test
  public void testWillRenderForComponentThatReturnsNonNull() {
    ComponentContext c = new ComponentContext(application);
    assertThat(c, Wrapper.create(c).delegate(mNonNullSpec).build()).willRender();
  }

  @Test
  public void testWillRenderForComponentWithSizeSpecThrowsException() {
    mExpectedException.expect(IllegalArgumentException.class);
    mExpectedException.expectMessage("@OnCreateLayoutWithSizeSpec");

    ComponentContext c = new ComponentContext(application);
    Component.willRender(c, Wrapper.create(c).delegate(mLayoutWithSizeSpec).build());
  }
}
