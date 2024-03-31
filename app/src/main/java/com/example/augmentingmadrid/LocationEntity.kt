package es.upm.btb.helloworldkt.persistence.room
import androidx.room.Entity
import androidx.room.PrimaryKey
@Entity
data class LocationEntity(
    @PrimaryKey(autoGenerate = true) val id: Int = 0,
    val latitude: Double,
    val longitude: Double,
    val timestamp: Long
)
