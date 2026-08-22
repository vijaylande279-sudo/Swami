export interface MenuCategory {
  id: number;
  name: string;
  displayOrder: number;
}

export interface MenuItem {
  id: number;
  categoryId: number;
  name: string;
  description: string;
  price: number;
  imageUrl: string | null;
  available: boolean;
  vegetarian: boolean;
}
