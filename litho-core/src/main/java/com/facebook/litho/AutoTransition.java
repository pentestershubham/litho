/*
 * Copyright (c) 2017-present, Facebook, Inc.
 * All rights reserved.
 *
 * This source code is licensed under the BSD-style license found in the
 * LICENSE file in the root directory of this source tree. An additional grant
 * of patent rights can be found in the PATENTS file in the same directory.
 */
package com.facebook.litho;

import java.util.ArrayList;

import com.facebook.litho.animation.AnimatedComponent;
import com.facebook.litho.animation.AnimatedProperty;
import com.facebook.litho.animation.AnimationBinding;
import com.facebook.litho.animation.AppearingComponent;
import com.facebook.litho.animation.ChangingComponent;
import com.facebook.litho.animation.ComponentProperty;
import com.facebook.litho.animation.DisappearingComponent;
import com.facebook.litho.animation.LazyFloatValue;
import com.facebook.litho.animation.LazyValue;
import com.facebook.litho.animation.SpringTransition;
import com.facebook.litho.animation.TransitionAnimationBinding;

/**
 * Defines how a property on a component should animate as it changes, allowing you to optionally
 * define appear-from values for appear animations and disappear-to values for disappear animations.
 */
public class AutoTransition {

  private static final TransitionAnimator DEFAULT_ANIMATOR = new SpringTransitionAnimator();

  /**
   * Class that knows how to create a {@link TransitionAnimationBinding} given a
   * {@link ComponentProperty}. This can be used to customize the type of animation using
   * {@link Builder#animator}.
   */
  public interface TransitionAnimator {

    /**
     * @return a {@link TransitionAnimationBinding} for the given {@link ComponentProperty} that
     * will animate the change in value on this property.
     */
    TransitionAnimationBinding createAnimation(ComponentProperty property);
  }

  /**
   * Creates a AutoTransition for the given property on the component with the given key.
   */
  public static AutoTransition.Builder create(String key) {
    return new AutoTransition.Builder(key);
  }

  /**
   * Creates a set of {@link AutoTransition}s.
   */
  public static AutoTransitionSet createSet(AutoTransition.Builder... transitions) {
    return new AutoTransitionSet(transitions);
  }

  private final String mTransitionKey;
  private final AnimatedProperty mAnimatedProperty;
  private final TransitionAnimator mTransitionAnimator;
  private final LazyValue mAppearFrom;
  private final LazyValue mDisappearTo;

  public AutoTransition(
      String transitionKey,
      AnimatedProperty animatedProperty,
      TransitionAnimator transitionAnimator,
      LazyValue appearFrom,
      LazyValue disappearTo) {
    mTransitionKey = transitionKey;
    mAnimatedProperty = animatedProperty;
    mTransitionAnimator = transitionAnimator;
    mAppearFrom = appearFrom;
    mDisappearTo = disappearTo;
  }

  boolean hasAppearAnimation() {
    return mAppearFrom != null;
  }

  boolean hasDisappearAnimation() {
    return mDisappearTo != null;
  }

  String getTransitionKey() {
    return mTransitionKey;
  }

  AnimationBinding createAppearAnimation() {
    if (!hasAppearAnimation()) {
      throw new RuntimeException(
          "Trying to create an appear animation when no from value was provided!");
    }

    final AppearingComponent component = new AppearingComponent(mTransitionKey);
    final ComponentProperty property = new AutoTransitionComponentProperty(
        component,
        mAnimatedProperty);
    final TransitionAnimationBinding animation = mTransitionAnimator.createAnimation(property);

    animation.addAppearFromValue(property, mAppearFrom);

    return animation;
  }

  AnimationBinding createDisappearAnimation() {
    if (!hasDisappearAnimation()) {
      throw new RuntimeException(
          "Trying to create an disappear animation when no to value was provided!");
    }

    final DisappearingComponent component = new DisappearingComponent(mTransitionKey);
    final ComponentProperty property = new AutoTransitionComponentProperty(
        component,
        mAnimatedProperty);
    final TransitionAnimationBinding animation = mTransitionAnimator.createAnimation(property);

    animation.addDisappearToValue(property, mDisappearTo);

    return animation;
  }

  AnimationBinding createChangeAnimation() {
    final ChangingComponent component = new ChangingComponent(mTransitionKey);
    final ComponentProperty property = new AutoTransitionComponentProperty(
        component,
        mAnimatedProperty);
    final TransitionAnimationBinding animation = mTransitionAnimator.createAnimation(property);

    return animation;
  }

  public static class Builder {

    private final String mKey;
    private final ArrayList<AutoTransition> mBuiltTransitions = new ArrayList<>();

    private AnimatedProperty mAnimatedProperty;
    private TransitionAnimator mTransitionAnimator = DEFAULT_ANIMATOR;
    private LazyValue mAppearFrom;
    private LazyValue mDisappearTo;

    Builder(String key) {
      mKey = key;
    }

    public Builder animate(AnimatedProperty property) {
      maybeCommitCurrentBuilder();
      mAnimatedProperty = property;
      return this;
    }

    /**
     * Use to define the {@link TransitionAnimator} that drives the animation. The default is a
     * spring.
     */
    public Builder animator(TransitionAnimator animator) {
      mTransitionAnimator = animator;
      return this;
    }

    /**
     * Define where appear animations should start from.
     *
     * @see LazyFloatValue
     * @see com.facebook.litho.animation.LazyDimensionValue
     */
    public Builder appearFrom(LazyValue value) {
      mAppearFrom = value;
      return this;
    }

    /**
     * Define where disappear animations should end at.
     *
     * @see LazyFloatValue
     * @see com.facebook.litho.animation.LazyDimensionValue
     */
    public Builder disappearTo(LazyValue value) {
      mDisappearTo = value;
      return this;
    }

    /**
     * Define a constant value where appear animations should start from.
     */
    public Builder appearFrom(float value) {
      mAppearFrom = new LazyFloatValue(value);
      return this;
    }

    /**
     * Define a constant value where disappear animations should end at.
     */
    public Builder disappearTo(float value) {
      mDisappearTo = new LazyFloatValue(value);
      return this;
    }

    ArrayList<AutoTransition> getTransitions() {
      maybeCommitCurrentBuilder();
      return mBuiltTransitions;
    }

    private void maybeCommitCurrentBuilder() {
      if (mAnimatedProperty == null) {
        return;
      }
      mBuiltTransitions.add(
          new AutoTransition(
              mKey,
              mAnimatedProperty,
              mTransitionAnimator,
              mAppearFrom,
              mDisappearTo));
      mAnimatedProperty = null;
      mTransitionAnimator = DEFAULT_ANIMATOR;
      mAppearFrom = null;
      mDisappearTo = null;
    }
  }

  private static class AutoTransitionComponentProperty extends ComponentProperty {

    AutoTransitionComponentProperty(
        AnimatedComponent animatedComponent,
        AnimatedProperty property) {
      super(animatedComponent, property);
    }
  }

  /**
   * Creates spring-driven animations.
   */
  public static class SpringTransitionAnimator implements TransitionAnimator {

    @Override
    public TransitionAnimationBinding createAnimation(ComponentProperty property) {
      return new SpringTransition(property);
    }
  }
}
