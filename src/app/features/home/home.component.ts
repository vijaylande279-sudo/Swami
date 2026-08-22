import { Component, computed, signal } from '@angular/core';
import { RouterLink } from '@angular/router';

interface Dish {
  emoji: string;
  name: string;
  description: string;
  price: string;
}

const PAGE_SIZE = 6;

const FEATURED_DISHES: Dish[] = [
  // Starters
  { emoji: '🥟', name: 'Veg Manchurian', description: 'Crispy veggie balls in tangy sauce', price: '₹140' },
  { emoji: '🍗', name: 'Chicken Tikka', description: 'Tandoor grilled spiced chicken', price: '₹220' },
  { emoji: '🧆', name: 'Hara Bhara Kabab', description: 'Spinach and peas cutlets', price: '₹130' },
  { emoji: '🐟', name: 'Fish Fingers', description: 'Crispy battered fish strips', price: '₹200' },
  { emoji: '🧀', name: 'Paneer Tikka', description: 'Grilled cottage cheese with spices', price: '₹160' },
  { emoji: '🥣', name: 'Soup of the Day', description: 'Ask your waiter for today special', price: '₹90' },

  // Main Course
  { emoji: '🍛', name: 'Dal Makhani', description: 'Slow cooked black lentils in butter', price: '₹180' },
  { emoji: '🍚', name: 'Chicken Biryani', description: 'Fragrant basmati rice with chicken', price: '₹280' },
  { emoji: '🍖', name: 'Mutton Rogan Josh', description: 'Slow cooked mutton in Kashmiri spices', price: '₹350' },
  { emoji: '🥬', name: 'Palak Paneer', description: 'Cottage cheese in spinach gravy', price: '₹220' },
  { emoji: '🥚', name: 'Egg Curry', description: 'Boiled eggs in spicy onion gravy', price: '₹180' },
  { emoji: '🐠', name: 'Fish Curry', description: 'Fresh fish in coconut gravy', price: '₹300' },
  { emoji: '🫓', name: 'Chole Bhature', description: 'Spiced chickpeas with fried bread', price: '₹160' },
  { emoji: '🍚', name: 'Veg Pulao', description: 'Basmati rice with mixed vegetables', price: '₹160' },
  { emoji: '🍳', name: 'Chicken Fried Rice', description: 'Indo-Chinese style fried rice', price: '₹200' },
  { emoji: '🫓', name: 'Naan', description: 'Soft tandoor baked flatbread', price: '₹40' },
  { emoji: '🫓', name: 'Butter Roti', description: 'Whole wheat flatbread with butter', price: '₹30' },
  { emoji: '🍚', name: 'Jeera Rice', description: 'Basmati rice tempered with cumin', price: '₹120' },

  // Desserts
  { emoji: '🍡', name: 'Rasgulla', description: 'Soft spongy cottage cheese balls in syrup', price: '₹80' },
  { emoji: '🍮', name: 'Kheer', description: 'Creamy rice pudding with dry fruits', price: '₹90' },
  { emoji: '🥕', name: 'Gajar Halwa', description: 'Slow cooked carrot dessert with ghee', price: '₹100' },
  { emoji: '🍫', name: 'Brownie with Ice Cream', description: 'Warm chocolate brownie with vanilla scoop', price: '₹130' },
  { emoji: '🍨', name: 'Fruit Custard', description: 'Mixed fruits in creamy vanilla custard', price: '₹90' },
  { emoji: '🥨', name: 'Jalebi', description: 'Crispy spiral sweets soaked in sugar syrup', price: '₹70' },

  // Drinks
  { emoji: '☕', name: 'Cold Coffee', description: 'Chilled blended coffee with milk', price: '₹90' },
  { emoji: '🍵', name: 'Masala Chai', description: 'Spiced Indian tea', price: '₹40' },
  { emoji: '🍊', name: 'Fresh Orange Juice', description: 'Freshly squeezed orange juice', price: '₹80' },
  { emoji: '🍉', name: 'Watermelon Juice', description: 'Fresh chilled watermelon juice', price: '₹70' },
  { emoji: '🥛', name: 'Sweet Lassi', description: 'Chilled sweetened yogurt drink', price: '₹70' },
  { emoji: '🍹', name: 'Virgin Mojito', description: 'Mint lime soda with crushed ice', price: '₹90' },
  { emoji: '🥥', name: 'Coconut Water', description: 'Fresh tender coconut water', price: '₹60' },
  { emoji: '🥛', name: 'Buttermilk', description: 'Salted spiced chilled chaas', price: '₹40' },
  { emoji: '☕', name: 'Hot Chocolate', description: 'Rich creamy hot chocolate drink', price: '₹100' },
];

@Component({
  selector: 'app-home',
  standalone: true,
  imports: [RouterLink],
  templateUrl: './home.component.html',
})
export class HomeComponent {
  readonly year = new Date().getFullYear();

  private readonly allDishes = FEATURED_DISHES;
  readonly pageIndex = signal(0);

  readonly pageCount = Math.ceil(this.allDishes.length / PAGE_SIZE);
  readonly pageIndices = Array.from({ length: this.pageCount }, (_, i) => i);

  readonly visibleDishes = computed(() => {
    const start = this.pageIndex() * PAGE_SIZE;
    return this.allDishes.slice(start, start + PAGE_SIZE);
  });

  nextPage(): void {
    this.pageIndex.update(i => (i + 1) % this.pageCount);
  }

  prevPage(): void {
    this.pageIndex.update(i => (i - 1 + this.pageCount) % this.pageCount);
  }

  goToPage(index: number): void {
    this.pageIndex.set(index);
  }
}
