/* This program is free software: you can redistribute it and/or
 modify it under the terms of the GNU Lesser General Public License
 as published by the Free Software Foundation, either version 3 of
 the License, or (at your option) any later version.

 This program is distributed in the hope that it will be useful,
 but WITHOUT ANY WARRANTY; without even the implied warranty of
 MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 GNU General Public License for more details.

 You should have received a copy of the GNU General Public License
 along with this program.  If not, see <http://www.gnu.org/licenses/>. */

package com.conveyal.gtfs.model;

import com.conveyal.gtfs.GTFSFeed;

import java.io.DataInput;
import java.io.DataOutput;
import java.io.IOException;
import java.io.Serializable;
import java.util.Iterator;

/**
 * Represents a GTFS StopTime. Note that once created and saved in a feed, stop times are by convention immutable
 * because they are in a MapDB.
 */
public class StopTime extends Entity implements Serializable {

    /* StopTime cannot directly reference Trips or Stops because they would be serialized into the MapDB. */
    public String trip_id;
    public int    arrival_time = INT_MISSING;
    public int    departure_time = INT_MISSING;
    public String stop_id;
    public int    stop_sequence;
    public String stop_headsign;
    public int    pickup_type;
    public int    drop_off_type;
    public double shape_dist_traveled;
    public int    timepoint = INT_MISSING;

    public static class Loader extends Entity.Loader<StopTime> {

        public Loader(GTFSFeed feed) {
            super(feed, "stop_times");
        }

        @Override
        public void loadOneRow() throws IOException {
            StopTime st = new StopTime();
            st.trip_id        = getStringField("trip_id", true);
            // TODO: arrival_time and departure time are not required, but if one is present the other should be
            // also, if this is the first or last stop, they are both required
            st.arrival_time   = getTimeField("arrival_time", false);
            st.departure_time = getTimeField("departure_time", false);
            st.stop_id        = getStringField("stop_id", true);
            st.stop_sequence  = getIntField("stop_sequence", true, 0, Integer.MAX_VALUE);
            st.stop_headsign  = getStringField("stop_headsign", false);
            st.pickup_type    = getIntField("pickup_type", false, 0, 3); // TODO add ranges as parameters
            st.drop_off_type  = getIntField("drop_off_type", false, 0, 3);
            st.shape_dist_traveled = getDoubleField("shape_dist_traveled", false, 0D, Double.MAX_VALUE);
            st.timepoint      = getIntField("timepoint", false, 0, 1);
            st.feed = null; // this could circular-serialize the whole feed
            feed.stop_times.put(new Object[]{st.trip_id, st.stop_sequence}, st);

            /*
              Check referential integrity without storing references. StopTime cannot directly reference Trips or
              Stops because they would be serialized into the MapDB.
             */
            getRefField("trip_id", true, feed.trips);
            getRefField("stop_id", true, feed.stops);
        }

    }

    public static class Writer extends Entity.Writer<StopTime> {
        public Writer (GTFSFeed feed) {
            super(feed, "stop_times");
        }

        @Override
        protected void writeHeaders() throws IOException {
            writer.writeRecord(new String[] {"trip_id", "arrival_time", "departure_time", "stop_id", "stop_sequence", "stop_headsign",
                    "pickup_type", "drop_off_type", "shape_dist_traveled", "timepoint"});
        }

        @Override
        protected void writeOneRow(StopTime st) throws IOException {
            writeStringField(st.trip_id);
            writeTimeField(st.arrival_time);
            writeTimeField(st.departure_time);
            writeStringField(st.stop_id);
            writeIntField(st.stop_sequence);
            writeStringField(st.stop_headsign);
            writeIntField(st.pickup_type);
            writeIntField(st.drop_off_type);
            writeDoubleField(st.shape_dist_traveled);
            writeIntField(st.timepoint);
            endRecord();
        }

        @Override
        protected Iterator<StopTime> iterator() {
            return feed.stop_times.values().iterator();
        }


    }

    public static final class Serializer extends org.mapdb.Serializer<StopTime> implements Serializable {
        @Override
        public void serialize(DataOutput out, StopTime t) throws IOException {
            out.writeUTF(t.trip_id);
            out.writeInt(t.arrival_time);
            out.writeInt(t.departure_time);
            out.writeUTF(t.stop_id);
            out.writeInt(t.stop_sequence);
            out.writeUTF(t.stop_headsign);
            out.writeInt(t.pickup_type);
            out.writeInt(t.drop_off_type);
            out.writeDouble(t.shape_dist_traveled);
            out.writeInt(t.timepoint);
        }

        @Override
        public StopTime deserialize(DataInput in, int available) throws IOException {
            StopTime t = new StopTime();

            t.trip_id = in.readUTF();
            t.arrival_time = in.readInt();
            t.departure_time =  in.readInt();
            t.stop_id = in.readUTF();
            t.stop_sequence = in.readInt();
            t.stop_headsign = in.readUTF();
            t.pickup_type = in.readInt();
            t.drop_off_type = in.readInt();
            t.shape_dist_traveled = in.readDouble();
            t.timepoint = in.readInt();

            return t;
        }
    }
}
