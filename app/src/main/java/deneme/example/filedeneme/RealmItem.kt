package deneme.example.filedeneme

import io.realm.RealmObject
import io.realm.annotations.PrimaryKey

open class RealmItem(
    @PrimaryKey
     var id: String? = null,
    var follower: String? = null,
    var follows: String? = null
) : RealmObject() {}

