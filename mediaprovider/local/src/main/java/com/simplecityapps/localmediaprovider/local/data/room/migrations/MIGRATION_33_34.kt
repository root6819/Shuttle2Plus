package com.simplecityapps.localmediaprovider.local.data.room.migrations

import androidx.room.migration.Migration
import androidx.sqlite.db.SupportSQLiteDatabase

val MIGRATION_33_34 = object : Migration(33, 34) {
    override fun migrate(database: SupportSQLiteDatabase) {
        database.execSQL("CREATE TABLE IF NOT EXISTS songs2 (id INTEGER PRIMARY KEY AUTOINCREMENT NOT NULL, name TEXT, track INTEGER, disc INTEGER, duration INTEGER NOT NULL, year INTEGER, path TEXT NOT NULL, albumArtist TEXT, artists TEXT NOT NULL DEFAULT '', album TEXT, size INTEGER NOT NULL, mimeType TEXT NOT NULL, genres TEXT NOT NULL DEFAULT '', lastModified INTEGER NOT NULL, playbackPosition INTEGER NOT NULL, playCount INTEGER NOT NULL, lastPlayed INTEGER, lastCompleted INTEGER, blacklisted INTEGER NOT NULL, mediaStoreId INTEGER, mediaProvider TEXT NOT NULL DEFAULT 'Shuttle', replayGainTrack REAL, replayGainAlbum REAL)")
        database.execSQL("CREATE UNIQUE INDEX IF NOT EXISTS index_songs2_path ON songs2 (path)")
        database.execSQL(
            "INSERT INTO songs2 (id, name, track, disc, duration, year, path, albumArtist, album, size, mimeType, genres, lastModified, playbackPosition, playCount, lastPlayed, lastCompleted, blacklisted, mediaStoreId, mediaProvider, replayGainTrack, replayGainAlbum) " +
                    "SELECT songs.id, songs.name, track, disc, duration, year, path, albumArtist, album, size, mimeType, genres, lastModified, playbackPosition, playCount, lastPlayed, lastCompleted, blacklisted, mediaStoreId, mediaProvider, replayGainTrack, replayGainAlbum " +
                    "FROM songs"
        )
        database.execSQL("DROP TABLE songs")
        database.execSQL("ALTER TABLE songs2 RENAME TO songs")

        database.execSQL("DROP VIEW `AlbumData`")
        database.execSQL("DROP VIEW `AlbumArtistData`")
    }
}