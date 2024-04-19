import js from "@eslint/js";
import stylisticJs from '@stylistic/eslint-plugin-js'

export default [
  js.configs.recommended,
  {
    plugins: {
      '@stylistic/js': stylisticJs
    },
    languageOptions: {
      globals: {
        __dirname: "readonly",
        console: "readonly",
        process: "readonly",
        require: "readonly",
      }
    },
    rules: {
      // Never allow trailing commas on lists
      "@stylistic/js/comma-dangle": ["error", "never"],

      // Align based on key values
      "@stylistic/js/key-spacing": ["error", { "align": "value" }],

      // Max line length should be 120
      "@stylistic/js/max-len": ["error", { "code": 120 }],

      // Allow multiple spaces when declaring requires
      "@stylistic/js/no-multi-spaces": ["error", { "exceptions": { "VariableDeclarator": true } }],

      // Always force spacing between curly braces
      "@stylistic/js/object-curly-spacing": ["error", "always"],
    }
  }
];
