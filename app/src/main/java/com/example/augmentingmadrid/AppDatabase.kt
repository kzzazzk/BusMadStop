package es.upm.btb.helloworldkt.persistence.room
import androidx.room.Database
import androidx.room.RoomDatabase
@Database(entities = [LocationEntity::class], version = 1)
abstract class AppDatabase : RoomDatabase() {
    abstract fun locationDao(): ILocationDao
}
