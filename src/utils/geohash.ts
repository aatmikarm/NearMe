// src/utils/geohash.ts
// A simplified geohashing implementation for proximity detection

// Encode a latitude/longitude pair into a geohash string
export function encodeGeohash(latitude: number, longitude: number, precision: number = 9): string {
    const BASE32 = '0123456789bcdefghjkmnpqrstuvwxyz';
    let geohash = '';
    
    let minLat = -90;
    let maxLat = 90;
    let minLng = -180;
    let maxLng = 180;
    
    let bit = 0;
    let idx = 0;
    
    while (geohash.length < precision) {
      if (bit % 2 === 0) {
        // Process longitude
        const midLng = (minLng + maxLng) / 2;
        if (longitude >= midLng) {
          idx = idx * 2 + 1;
          minLng = midLng;
        } else {
          idx = idx * 2;
          maxLng = midLng;
        }
      } else {
        // Process latitude
        const midLat = (minLat + maxLat) / 2;
        if (latitude >= midLat) {
          idx = idx * 2 + 1;
          minLat = midLat;
        } else {
          idx = idx * 2;
          maxLat = midLat;
        }
      }
      
      bit++;
      
      if (bit === 5) {
        geohash += BASE32[idx];
        bit = 0;
        idx = 0;
      }
    }
    
    return geohash;
  }
  
  // Calculate neighbor geohashes in all directions
  export function getNeighbors(geohash: string): string[] {
    const NEIGHBORS = {
      n: ['p0', 'p1', 'p2', 'p3', 'p4', 'p5', 'p6', 'p7', 'p8', 'p9', 'pb', 'pc', 'pd', 'pe', 'pf', 'pg', 'ph', 'pj', 'pk', 'pm', 'pn', 'pp', 'pq', 'pr', 'ps', 'pt', 'pu', 'pv', 'pw', 'px', 'py', 'pz'],
      e: ['b0', 'b1', 'b2', 'b3', 'b4', 'b5', 'b6', 'b7', 'b8', 'b9', 'bb', 'bc', 'bd', 'be', 'bf', 'bg', 'bh', 'bj', 'bk', 'bm', 'bn', 'bp', 'bq', 'br', 'bs', 'bt', 'bu', 'bv', 'bw', 'bx', 'by', 'bz'],
      s: ['h0', 'h1', 'h2', 'h3', 'h4', 'h5', 'h6', 'h7', 'h8', 'h9', 'hb', 'hc', 'hd', 'he', 'hf', 'hg', 'hh', 'hj', 'hk', 'hm', 'hn', 'hp', 'hq', 'hr', 'hs', 'ht', 'hu', 'hv', 'hw', 'hx', 'hy', 'hz'],
      w: ['8p', '8r', '8x', '8z', '2p', '2r', '2x', '2z', 'sp', 'sr', 'sx', 'sz']
    };
  
    const BORDERS = {
      n: ['prz', 'p2z', 'p8z', 'pbz'],
      e: ['b00', 'b02', 'b08', 'b0b'],
      s: ['h00', 'h02', 'h08', 'h0b'],
      w: ['8p0', '8p2', '8p8', '8pb']
    };
  
    // This is a simplified implementation
    // For a real implementation, you would check borders and handle edge cases
    
    // For now, just return a simple set of neighbors
    return [
      geohash + NEIGHBORS.n[0],
      geohash + NEIGHBORS.e[0],
      geohash + NEIGHBORS.s[0],
      geohash + NEIGHBORS.w[0],
      geohash
    ];
  }
  
  // Calculate the distance between two points in meters
  export function calculateDistance(
    lat1: number, 
    lon1: number, 
    lat2: number, 
    lon2: number
  ): number {
    const R = 6371e3; // Earth's radius in meters
    const φ1 = (lat1 * Math.PI) / 180;
    const φ2 = (lat2 * Math.PI) / 180;
    const Δφ = ((lat2 - lat1) * Math.PI) / 180;
    const Δλ = ((lon2 - lon1) * Math.PI) / 180;
  
    const a =
      Math.sin(Δφ / 2) * Math.sin(Δφ / 2) +
      Math.cos(φ1) * Math.cos(φ2) * Math.sin(Δλ / 2) * Math.sin(Δλ / 2);
    const c = 2 * Math.atan2(Math.sqrt(a), Math.sqrt(1 - a));
  
    return R * c;
  }
  
  // Generate query bounds for a radius search
  export function getGeohashQueryBounds(
    latitude: number,
    longitude: number,
    radiusInMeters: number
  ): [string, string][] {
    // This is a simplified implementation
    // For a real implementation, you would calculate precise bounds
    
    // For now, just generate geohashes with reduced precision for the area
    const precision = radiusInMeters <= 100 ? 7 : radiusInMeters <= 1000 ? 6 : 5;
    const centerGeohash = encodeGeohash(latitude, longitude, precision);
    const neighbors = getNeighbors(centerGeohash);
    
    // Convert to range queries
    return neighbors.map(hash => [hash, hash + '~']);
  }