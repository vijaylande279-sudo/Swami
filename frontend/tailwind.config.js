/** @type {import('tailwindcss').Config} */
module.exports = {
  content: ['./src/**/*.{html,ts}'],
  theme: {
    extend: {
      colors: {
        brand: {
          50: '#fff7ed',
          100: '#ffedd5',
          200: '#fed7aa',
          300: '#fdba74',
          400: '#fb923c',
          500: '#f97316',
          600: '#ea580c',
          700: '#c2410c',
          800: '#9a3412',
          900: '#7c2d12',
        },
        status: {
          available: '#16a34a',
          occupied: '#dc2626',
          reserved: '#d97706',
          pending: '#2563eb',
          preparing: '#d97706',
          ready: '#16a34a',
          served: '#6b7280',
        },
        surface: {
          DEFAULT: '#0f0f0f',
          card: '#1a1a1a',
          border: '#2a2a2a',
          hover: '#242424',
        },
      },
      spacing: {
        tap: '2.75rem',
      },
      minHeight: {
        tap: '2.75rem',
      },
      minWidth: {
        tap: '2.75rem',
      },
    },
  },
  darkMode: 'class',
  plugins: [],
};
