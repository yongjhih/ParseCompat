# Parse Compat

* `ParseRecyclerAdapter<T, VH> extends RecyclerView.Adapter<VH>`

## Usage

```java
adapter = new ParseRecyclerAdapter<Post, MyViewHolder>(this, () -> {
    ParseQuery<Post> query = Post.getQuery();
    query.include(Post.USER);
    return query;
}) {
    @Override public RecyclerView.ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        super.onCreateViewHolder(parent, viewType);
        final View view = LayoutInflater.from(context).inflate(R.layout.item_view, parent, false);
        ...
        return viewHolder;
    }
};

adapter.onBindViewHolder((viewHolder, position, post) -> {
    ...
});
```

## Installation

Via bintray

```gradle
repositories {
    maven {
        url 'https://dl.bintray.com/yongjhih/maven/'
    }
}

dependencies {
    compile 'com.infstory:parse-compat:1.0.5'
}
```

Or via jitpack.io

```gradle
repositories {
    maven {
        url "https://jitpack.io"
    }
}

dependencies {
    compile 'com.github.yongjhih:ParseCompat:1.0.0'
}
```

## LICENSE

Copyright 2015 8tory, Inc.

Licensed under the Apache License, Version 2.0 (the "License"); you may not use this file except in compliance with the License. You may obtain a copy of the License at

http://www.apache.org/licenses/LICENSE-2.0

Unless required by applicable law or agreed to in writing, software distributed under the License is distributed on an "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied. See the License for the specific language governing permissions and limitations under the License.
