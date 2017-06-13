# android-nestedscroll-maps
A `NestedScrollingChild` aware straight forward replacement for Google's `MapFragment` and `SupportMapFragment` fragments that works well with `CoordinatorLayout`, `AppBarLayout` and `CollapsingToolbarLayout` widgets from Google's Design library.

## Import
On your `build.gradle` add:
```
    dependencies {
        compile 'com.github.gmazzo:nestedscroll-maps:0.3'
    }
```
[ ![Download](https://api.bintray.com/packages/gmazzo/maven/android-nestedscroll-maps/images/download.svg) ](https://bintray.com/gmazzo/maven/android-nestedscroll-maps/_latestVersion)

## Usage
Just replace `MapFragment` and `SupportMapFragment` with `NestedScrollMapFragment` and `SupportNestedScrollMapFragment` respectively

## Demo
NestedScrollMapFragment|MapFragment
---|---
![Nested Demo](screenshots/demoNested.gif)|![Regular Demo](screenshots/demoRegular.gif)
