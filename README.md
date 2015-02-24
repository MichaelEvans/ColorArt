##About

ColorArt is a library that uses an image to create a themed image/text display. It's a port of the idea found on the [Panic Blog](http://www.panic.com/blog/itunes-11-and-colors/) to work on Android.

##Usage

(ColorArt is supported on Android 2.1+.)

Add ColorArt as a dependency to your build.gradle file:

```
compile 'org.michaelevans.colorart:library:0.0.3'﻿
```

Then you can use the library like this:

```
// get a bitmap and analyze it
Bitmap bitmap = BitmapFactory.decodeResource(getResources(), R.drawable.album);
ColorArt colorArt = new ColorArt(bitmap);

// get the colors
colorArt.getBackgroundColor()
colorArt.getPrimaryColor()
colorArt.getSecondaryColor()
colorArt.getDetailColor()
```

###FadingImageView

```
mFadingImageView.setBackgroundColor(colorArt.getBackgroundColor(), FadingImageView.FadeSide.LEFT);
```
This will set the fading edge on the left side, with that background color. You can also enable/disable the fade with:

```
mImageView.setFadeEnabled(true/false);
```


##Screenshots

![Jim Noir](https://github.com/MichaelEvans/ColorArt/raw/master/img/jim_noir.png)
![Ping Pong Orchestra](https://github.com/MichaelEvans/ColorArt/raw/master/img/ping_pong_orchestra.png)
![Hotel Shampoo](https://github.com/MichaelEvans/ColorArt/raw/master/img/hotel_shampoo.png)

##License

```
Copyright 2015 Michael Evans

Licensed under the Apache License, Version 2.0 (the "License");
you may not use this file except in compliance with the License.
You may obtain a copy of the License at

   http://www.apache.org/licenses/LICENSE-2.0

Unless required by applicable law or agreed to in writing, software
distributed under the License is distributed on an "AS IS" BASIS,
WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
See the License for the specific language governing permissions and
limitations under the License.
```

[![Bitdeli Badge](https://d2weczhvl823v0.cloudfront.net/MichaelEvans/colorart/trend.png)](https://bitdeli.com/free "Bitdeli Badge")
