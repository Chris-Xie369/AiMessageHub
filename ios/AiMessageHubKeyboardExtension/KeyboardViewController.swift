import UIKit

final class KeyboardViewController: UIInputViewController {
    private let suggestionProvider = SuggestionProvider()
    private let resultLabel = UILabel()
    private let suggestionsStack = UIStackView()
    private var variantButtons: [UIButton] = []

    override func viewDidLoad() {
        super.viewDidLoad()
        setupLayout()
    }

    private func setupLayout() {
        let stack = UIStackView()
        stack.axis = .vertical
        stack.spacing = 8
        stack.translatesAutoresizingMaskIntoConstraints = false
        view.addSubview(stack)

        let title = UILabel()
        title.text = "AI 建议"
        title.font = .preferredFont(forTextStyle: .headline)

        let generateButton = UIButton(type: .system)
        generateButton.setTitle("分析剪贴板", for: .normal)
        generateButton.addTarget(self, action: #selector(generate), for: .touchUpInside)

        resultLabel.numberOfLines = 0
        resultLabel.font = .preferredFont(forTextStyle: .body)

        suggestionsStack.axis = .vertical
        suggestionsStack.spacing = 8
        suggestionsStack.translatesAutoresizingMaskIntoConstraints = false

        let nextButton = UIButton(type: .system)
        nextButton.setTitle("切换键盘", for: .normal)
        nextButton.addTarget(self, action: #selector(advanceKeyboard), for: .touchUpInside)

        stack.addArrangedSubview(title)
        stack.addArrangedSubview(generateButton)
        stack.addArrangedSubview(resultLabel)
        stack.addArrangedSubview(suggestionsStack)
        stack.addArrangedSubview(nextButton)

        NSLayoutConstraint.activate([
            stack.leadingAnchor.constraint(equalTo: view.leadingAnchor, constant: 12),
            stack.trailingAnchor.constraint(equalTo: view.trailingAnchor, constant: -12),
            stack.topAnchor.constraint(equalTo: view.topAnchor, constant: 12),
            stack.bottomAnchor.constraint(lessThanOrEqualTo: view.bottomAnchor, constant: -12),
            suggestionsStack.leadingAnchor.constraint(equalTo: view.leadingAnchor, constant: 12),
            suggestionsStack.trailingAnchor.constraint(equalTo: view.trailingAnchor, constant: -12),
        ])
    }

    @objc private func generate() {
        let clipboard = UIPasteboard.general.string ?? ""
        guard !clipboard.isEmpty else {
            resultLabel.text = "剪贴板没有文本"
            resultLabel.textColor = .systemRed
            return
        }
        let message = ChatMessage(
            app: "keyboard",
            conversationId: "clipboard",
            contact: "剪贴板",
            text: clipboard
        )
        Task { @MainActor in
            let result = await suggestionProvider.suggest(
                contact: "剪贴板",
                history: [message],
                settings: AppConfig.settings
            )
            render(result)
        }
    }

    private func render(_ result: SuggestionResult) {
        variantButtons.forEach { $0.removeFromSuperview() }
        variantButtons.removeAll()

        if let error = result.error {
            resultLabel.text = error
            resultLabel.textColor = .systemRed
            return
        }
        resultLabel.text = "\(result.variants.count) 条建议"
        resultLabel.textColor = .label

        for variant in result.variants {
            let button = UIButton(type: .system)
            button.setTitle(variant, for: .normal)
            button.titleLabel?.numberOfLines = 0
            button.addTarget(self, action: #selector(insertVariant(_:)), for: .touchUpInside)
            suggestionsStack.addArrangedSubview(button)
            variantButtons.append(button)
        }
    }

    @objc private func insertVariant(_ sender: UIButton) {
        guard let text = sender.titleLabel?.text else { return }
        textDocumentProxy.insertText(text)
        variantButtons.forEach { $0.removeFromSuperview() }
        variantButtons.removeAll()
    }

    @objc private func advanceKeyboard() {
        advanceToNextInputMode()
    }
}
