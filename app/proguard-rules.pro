-keep public class com.org.smallcircle.model.ModelProduct {
    public <init>();
}

-keep public class com.org.smallcircle.model.ModelImageSlider {
    public <init>();
}

-keep class com.org.smallcircle.model.** { *; }

-keep class com.org.smallcircle.model.ModelProduct {
    <init>();
    <fields>;
}

-keep class com.org.smallcircle.model.ModelImageSlider {
    <init>();
    <fields>;
}
