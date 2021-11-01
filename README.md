[![](https://jitpack.io/v/adventurerDW/eqchartview.svg)](https://jitpack.io/#adventurerDW/eqchartview)

# eqchartview
滤波器折线图、EQ、支持增点删点

将它添加到存储库末尾的根 build.gradle 中：
allprojects {
		repositories {
			...
			maven { url 'https://jitpack.io' }
			//如果不行试试这个
			maven { url 'https://www.jitpack.io' }
			//如果不行试试这个
			maven { url 'http://jitpack.io' }
		}
}

# ..
dependencies {
    implementation 'com.github.adventurerDW:eqchartview:TAG'
}
