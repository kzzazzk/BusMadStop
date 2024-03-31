package es.upm.btb.helloworldkt.persistence.room
import androidx.room.Dao
import androidx.room.Insert
import androidx.room.Query
@Dao
interface ILocationDao {
    @Insert
    fun insertLocation(locationEntity: LocationEntity)
    @Query("SELECT * FROM LocationEntity")
    fun getAllLocations(): List<LocationEntity>
    @Query("SELECT COUNT(*) FROM LocationEntity")
    fun getCount(): Int
    @Query("DELETE FROM LocationEntity WHERE timestamp = :timestamp")
    fun deleteLocationByTimestamp(timestamp: Long)
}
