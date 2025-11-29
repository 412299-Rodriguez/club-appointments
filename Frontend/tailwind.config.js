/** @type {import('tailwindcss').Config} */
module.exports = {
  content: [
    "./src/**/*.{html,ts}",
  ],
  theme: {
    extend: {
      colors: {
        'primary-bg': '#1a1f37',
        'secondary-bg': '#252d4a',
        'accent-blue': '#4169e1',
        'accent-red': '#dc143c',
        'text-primary': '#ffffff',
        'text-secondary': '#9ca3af',
        'card-bg': '#2d3555',
        'input-bg': '#384160',
        'border-color': '#3f4a6b',
      },
    },
  },
  plugins: [],
}
