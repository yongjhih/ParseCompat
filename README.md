# Parse Compat
[![rxparse.png](art/parse.png)](art/parse.png)

* `ParseRecyclerAdapter<T, VH> extends RecyclerView.Adapter<VH>`

## Usage

User List, directly:

```java
ParseRecyclerAdapter<ParseUser, MyViewHolder> adapter = ParseRecyclerAdapter.from(activity)
    .query(() -> ParseUser.getQuery())
    .createViewHolder((parent, viewType) -> {
        final View view = LayoutInflater.from(context).inflate(R.layout.item_view, parent, false);
        return MyViewHolder.create(view, parent, viewType);
    })
    .bindViewHolder((viewHolder, position, parseUser) -> viewHolder.bind(position, parseUser));
```

Or inheritance ParseRecyclerAdapter

```java
public class ParseUserRecyclerAdapter extends ParseRecyclerAdapter<ParseUser, MyViewHolder>() {
    ParseUserRecyclerAdapter(Context context) {
        super(context);

        query(() -> ParseUser.getQuery())
        .createViewHolder((parent, viewType) -> {
            final View view = LayoutInflater.from(context).inflate(R.layout.item_view, parent, false);
            return MyViewHolder.create(view, parent, viewType);
        })
        .bindViewHolder((viewHolder, position, parseUser) -> viewHolder.bind(position, parseUser));
    }
}

ParseRecyclerAdapter adapter = ParseRecyclerAdapter.from(activity);
```

## Installation

Via jcenter

```gradle
repositories {
    jcenter()
}

dependencies {
    compile 'com.infstory:parse-compat:1.0.1'
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
    compile 'com.github.yongjhih:ParseCompat:1.0.1'
}
```

## LICENSE

Copyright 2015 8tory, Inc.

Licensed under the Apache License, Version 2.0 (the "License"); you may not use this file except in compliance with the License. You may obtain a copy of the License at

http://www.apache.org/licenses/LICENSE-2.0

Unless required by applicable law or agreed to in writing, software distributed under the License is distributed on an "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied. See the License for the specific language governing permissions and limitations under the License.
