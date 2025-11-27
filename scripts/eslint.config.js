import js from "@eslint/js";
import stylistic from '@stylistic/eslint-plugin'

export default [
  js.configs.recommended,
  {
    plugins: {
      '@stylistic': stylistic
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
      "@stylistic/comma-dangle": ["error", "never"],

      // Align based on key values
      "@stylistic/key-spacing": ["error", { "align": "value" }],

      // Max line length should be 120
      "@stylistic/max-len": ["error", { "code": 120 }],

      // Allow multiple spaces when declaring requires
      "@stylistic/no-multi-spaces": ["error", { "exceptions": { "VariableDeclarator": true } }],

      // Always force spacing between curly braces
      "@stylistic/object-curly-spacing": ["error", "always"],
    }
  }
];
