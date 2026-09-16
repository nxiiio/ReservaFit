export type Gym = {
  id: number;
  name: string;
  comuna: string | null;
  address: string | null;
  description: string | null;
  imageUrl: string | null;
};

export type SlotAvailability = {
  id: number;
  startTime: string; // "HH:mm:ss"
  endTime: string;
  available: boolean;
};
