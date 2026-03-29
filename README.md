🏗️ Copper Golem Builder (AI-Powered Minecraft Construction)
Copper Golem Builder is a Minecraft mod that brings an elite architectural AI to your survival world. By giving a Copper Golem an API key,it becomes capable of interpreting natural language prompts and constructing complex structures block-by-block in real-time. 🤖🧱

✨ Features
Natural Language to Build: Just tell your golem what to build (e.g., "Build a small spruce cabin with a chimney") and watch it work.

Multi-Model Support: Toggle between Claude 3.5 Sonnet/Haiku and Gemini 2.5 Flash/Pro via OpenRouter.

Smart Property Handling: Automatically handles complex blockstate properties like facing, axis, half, and shape for perfect stairs, logs, and redstone.

Asynchronous Construction: The golem parses JSON build steps and executes them in order (Y-ascending) to ensure structural integrity.

In-Game Configuration: Easy-to-use UI for setting your API Key and selecting your preferred AI model.

🚀 Getting Started
Craft/Summon a Copper Golem.

Right-click the golem to open the configuration menu.

Set your API Key (OpenRouter) and select a model.

Give a prompt and clear some space!

🛠️ Technical Overview
The mod uses a GolemAIBrain system to communicate with the OpenRouter API. It sends a specialized system prompt that forces the AI to output a raw JSON array of block data. This data is then parsed into BuildStep records and added to the golem's construction queue.

Note: Requires an active internet connection and an OpenRouter API key.
