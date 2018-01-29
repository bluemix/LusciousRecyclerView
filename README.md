# LusciousRecyclerView
RecyclerView with custom LayoutManager 
 
![image](https://github.com/ChenLittlePing/RecyclerCoverFlow/blob/master/gif/demo.gif)

<p>Gradle依赖 compile 'com.chenlittleping:recyclercoverflow:1.0.5'

# How to
### Define it in the XML
```xml
    <recycler.lusciou.LusciousRecyclerView
            android:id="@+id/list"
            android:layout_width="match_parent"
            android:layout_height="match_parent">
    </recycler.lusciou.LusciousRecyclerView>
```
### 2，Activity中初始化，其中Adapter与RecyclerView的Adapter完全一致
```java
    mList = (RecyclerCoverFlow) findViewById(R.id.list);
    //        mList.setFlatFlow(true); //平面滚动
    mList.setAdapter(new Adapter(this));
    mList.setOnItemSelectedListener(new CoverFlowLayoutManger.OnSelected() {
        @Override
        public void onItemSelected(int position) {
            ((TextView)findViewById(R.id.index)).setText((position+1)+"/"+mList.getLayoutManager().getItemCount());
        }
    });
```
